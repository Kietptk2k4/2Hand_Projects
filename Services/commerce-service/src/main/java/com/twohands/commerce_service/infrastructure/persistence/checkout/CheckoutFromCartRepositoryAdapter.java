package com.twohands.commerce_service.infrastructure.persistence.checkout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCreatedOutboxService;
import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.address.UserAddressRepository;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ProductPriceCalculator;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRepository;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRequest;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
import com.twohands.commerce_service.domain.checkout.ShippingFeeAllocator;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class CheckoutFromCartRepositoryAdapter implements CheckoutFromCartRepository {

    private static final Logger log = LoggerFactory.getLogger(CheckoutFromCartRepositoryAdapter.class);
    private static final String CHANGED_BY = "BUYER";
    private static final String CHECKOUT_NOTE = "CHECKOUT_FROM_CART";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final SellerShippingProfileRepository sellerShippingProfileRepository;
    private final ShippingFeeQuoteService shippingFeeQuoteService;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderCreatedOutboxService orderCreatedOutboxService;
    private final InventoryReservedOutboxService inventoryReservedOutboxService;
    private final ObjectMapper objectMapper;
    private final int paymentTtlMinutes;

    public CheckoutFromCartRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserAddressRepository userAddressRepository,
            SellerShippingProfileRepository sellerShippingProfileRepository,
            ShippingFeeQuoteService shippingFeeQuoteService,
            OutboxEventRepository outboxEventRepository,
            OrderCreatedOutboxService orderCreatedOutboxService,
            InventoryReservedOutboxService inventoryReservedOutboxService,
            ObjectMapper objectMapper,
            @Value("${commerce.jobs.auto-cancel-unpaid-order.order-ttl-minutes:30}") int paymentTtlMinutes
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userAddressRepository = userAddressRepository;
        this.sellerShippingProfileRepository = sellerShippingProfileRepository;
        this.shippingFeeQuoteService = shippingFeeQuoteService;
        this.outboxEventRepository = outboxEventRepository;
        this.orderCreatedOutboxService = orderCreatedOutboxService;
        this.inventoryReservedOutboxService = inventoryReservedOutboxService;
        this.objectMapper = objectMapper;
        this.paymentTtlMinutes = paymentTtlMinutes;
    }

    @Override
    @Transactional
    public CheckoutFromCartResult checkout(CheckoutFromCartRequest request) {
        Instant now = Instant.now();

        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            CheckoutFromCartResult existing = findByIdempotencyKey(request.buyerId(), request.idempotencyKey().trim());
            if (existing != null) {
                return existing;
            }
        }

        Cart cart = cartRepository.findByUserId(request.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart not found for user"));

        List<CartItem> cartItems = loadCartItems(cart.id(), request.cartItemIds());
        UserAddress address = userAddressRepository.findByIdAndUserId(request.addressId(), request.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        ShipmentType shipmentType = request.shipmentType() == null ? ShipmentType.STANDARD : request.shipmentType();
        List<PreparedLine> lines = buildAndValidateLines(cartItems, now);
        BigDecimal totalAmount = lines.stream().map(PreparedLine::itemTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = calculateShippingFee(lines, address, shipmentType);
        allocateShippingFees(lines, shippingFee);
        BigDecimal finalAmount = totalAmount.add(shippingFee);

        Map<UUID, Integer> quantityByProduct = aggregateQuantityByProduct(lines);
        lockInventories(quantityByProduct.keySet().stream().sorted().toList());
        reserveInventoryOrThrow(quantityByProduct, now);

        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OrderStatus orderStatus = resolveOrderStatus(request.paymentMethod());
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        insertOrder(orderId, request.buyerId(), totalAmount, finalAmount, request.paymentMethod(), orderStatus, now);
        insertOrderItems(orderId, lines, now);
        insertPayment(
                paymentId,
                orderId,
                request.buyerId(),
                finalAmount,
                request.paymentMethod(),
                request.idempotencyKey(),
                now
        );
        insertOrderStatusHistory(orderId, null, orderStatus.name(), now);
        insertPaymentStatusHistory(paymentId, null, paymentStatus.name(), now);

        List<OrderItemQuantity> reservedItems = quantityByProduct.entrySet().stream()
                .map(entry -> new OrderItemQuantity(entry.getKey(), entry.getKey(), entry.getValue()))
                .toList();

        outboxEventRepository.save(orderCreatedOutboxService.build(
                orderId,
                request.buyerId(),
                finalAmount,
                request.paymentMethod().name(),
                now
        ));
        outboxEventRepository.save(inventoryReservedOutboxService.build(orderId, reservedItems, now));

        String payosCheckoutUrl = resolvePayosCheckoutUrl(request.paymentMethod());

        return new CheckoutFromCartResult(
                orderId,
                paymentId,
                request.paymentMethod(),
                paymentStatus,
                orderStatus,
                finalAmount,
                payosCheckoutUrl,
                false
        );
    }

    private CheckoutFromCartResult findByIdempotencyKey(UUID buyerId, String idempotencyKey) {
        String sql = """
                SELECT o.id AS order_id,
                       p.id AS payment_id,
                       o.status AS order_status,
                       p.status AS payment_status,
                       p.payment_method,
                       o.final_amount,
                       p.payos_checkout_url
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.idempotency_key = :idempotencyKey
                  AND o.buyer_id = :buyerId
                """;
        List<CheckoutFromCartResult> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idempotencyKey", idempotencyKey)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> new CheckoutFromCartResult(
                        UUID.fromString(rs.getString("order_id")),
                        UUID.fromString(rs.getString("payment_id")),
                        PaymentMethod.valueOf(rs.getString("payment_method")),
                        PaymentStatus.valueOf(rs.getString("payment_status")),
                        OrderStatus.valueOf(rs.getString("order_status")),
                        rs.getBigDecimal("final_amount"),
                        rs.getString("payos_checkout_url"),
                        true
                )
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    private List<CartItem> loadCartItems(UUID cartId, List<UUID> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "At least one cart item is required",
                    "cart_item_ids",
                    "must not be empty"
            );
        }
        List<UUID> distinctIds = cartItemIds.stream().distinct().toList();
        List<CartItem> loaded = cartItemRepository.findByCartIdAndIds(cartId, distinctIds);
        if (loaded.size() != distinctIds.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "One or more cart items were not found");
        }
        for (CartItem item : loaded) {
            if (item.status() == CartItemStatus.REMOVED || item.status() == CartItemStatus.INVALID_PRODUCT) {
                throw new AppException(ErrorCode.INVALID_CART_ITEM, "Cart item is not available for checkout");
            }
            if (item.quantity() <= 0) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Cart item quantity must be greater than 0");
            }
        }
        return loaded;
    }

    private List<PreparedLine> buildAndValidateLines(List<CartItem> cartItems, Instant now) {
        List<PreparedLine> lines = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductCheckoutRow product = loadProductCheckoutRow(cartItem.productId(), now);
            validateProductForCheckout(product, cartItem.quantity());

            BigDecimal unitPrice = ProductPriceCalculator.effectivePrice(product.price(), product.salePrice());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.quantity()));

            lines.add(new PreparedLine(
                    cartItem.id(),
                    cartItem.productId(),
                    cartItem.sellerId(),
                    product.shopId(),
                    product.title(),
                    product.shopName(),
                    product.sku(),
                    product.imageUrl(),
                    product.attributesJson(),
                    unitPrice,
                    cartItem.quantity(),
                    itemTotal,
                    product.weightGram() * cartItem.quantity(),
                    BigDecimal.ZERO
            ));
        }
        return lines;
    }

    private ProductCheckoutRow loadProductCheckoutRow(UUID productId, Instant now) {
        String sql = """
                SELECT p.id,
                       p.seller_id,
                       p.shop_id,
                       p.title,
                       p.weight_gram,
                       p.status AS product_status,
                       s.shop_name,
                       s.status AS shop_status,
                       COALESCE(ss.is_vacation, FALSE) AS is_vacation,
                       pc.is_active AS category_active,
                       pi.stock_quantity,
                       pp.price,
                       pp.sale_price,
                       (
                           SELECT pm.media_url
                           FROM product_media pm
                           WHERE pm.product_id = p.id
                           ORDER BY pm.sort_order ASC
                           LIMIT 1
                       ) AS image_url,
                       (
                           SELECT pa.attribute_value
                           FROM product_attributes pa
                           WHERE pa.product_id = p.id AND pa.attribute_name = 'SKU'
                           LIMIT 1
                       ) AS sku,
                       (
                           SELECT COALESCE(jsonb_object_agg(pa.attribute_name, pa.attribute_value), '{}'::jsonb)::text
                           FROM product_attributes pa
                           WHERE pa.product_id = p.id
                       ) AS attributes_json
                FROM products p
                INNER JOIN seller_shops s ON s.id = p.shop_id
                INNER JOIN product_categories pc ON pc.id = p.category_id
                LEFT JOIN shop_settings ss ON ss.shop_id = p.shop_id
                INNER JOIN product_inventories pi ON pi.product_id = p.id
                LEFT JOIN LATERAL (
                    SELECT price, sale_price
                    FROM product_prices
                    WHERE product_id = p.id
                      AND start_at <= :now
                      AND (end_at IS NULL OR end_at > :now)
                    ORDER BY start_at DESC
                    LIMIT 1
                ) pp ON TRUE
                WHERE p.id = :productId
                """;
        List<ProductCheckoutRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("now", Timestamp.from(now)),
                (rs, rowNum) -> mapProductCheckoutRow(rs)
        );
        if (rows.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return rows.getFirst();
    }

    private void validateProductForCheckout(ProductCheckoutRow product, int quantity) {
        if (!ProductStatus.ACTIVE.name().equals(product.productStatus())) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Product is not available for purchase");
        }
        if (!ShopStatus.ACTIVE.name().equals(product.shopStatus())) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Shop is not available for purchase");
        }
        if (!product.categoryActive()) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Product category is not active");
        }
        if (product.isVacation()) {
            throw new AppException(ErrorCode.SHOP_VACATION);
        }
        if (product.price() == null) {
            throw new AppException(ErrorCode.ACTIVE_PRICE_MISSING);
        }
        if (product.stockQuantity() < quantity) {
            throw new AppException(ErrorCode.OUT_OF_STOCK, "Insufficient stock for checkout");
        }
    }

    private BigDecimal calculateShippingFee(List<PreparedLine> lines, UserAddress address, ShipmentType shipmentType) {
        Map<UUID, SellerGroup> groups = groupLinesBySeller(lines);

        Map<UUID, SellerShippingProfile> profiles = sellerShippingProfileRepository.findByShopIds(
                groups.values().stream().map(SellerGroup::shopId).distinct().toList()
        );

        BigDecimal totalShippingFee = BigDecimal.ZERO;
        for (SellerGroup group : groups.values()) {
            SellerShippingProfile profile = profiles.get(group.shopId());
            if (profile == null) {
                throw new AppException(
                        ErrorCode.SHIPPING_PROFILE_MISSING,
                        "Seller shipping profile is missing for shop " + group.shopId()
                );
            }
            ShippingGroupFeeQuote quote = shippingFeeQuoteService.quoteGroup(
                    profile,
                    address.provinceCode(),
                    address.districtCode(),
                    group.totalWeightGram(),
                    shipmentType
            );
            totalShippingFee = totalShippingFee.add(quote.shippingFee());
        }
        return totalShippingFee;
    }

    private void allocateShippingFees(List<PreparedLine> lines, BigDecimal totalShippingFee) {
        Map<UUID, SellerGroup> groups = groupLinesBySeller(lines);
        BigDecimal allocatedSum = BigDecimal.ZERO;
        List<SellerGroup> groupList = new ArrayList<>(groups.values());

        for (int g = 0; g < groupList.size(); g++) {
            SellerGroup group = groupList.get(g);
            BigDecimal groupSubtotal = group.lines().stream()
                    .map(PreparedLine::itemTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal groupFee;
            if (g == groupList.size() - 1) {
                groupFee = totalShippingFee.subtract(allocatedSum);
                if (groupFee.compareTo(BigDecimal.ZERO) < 0) {
                    groupFee = BigDecimal.ZERO;
                }
            } else if (totalShippingFee.compareTo(BigDecimal.ZERO) == 0 || groupSubtotal.compareTo(BigDecimal.ZERO) == 0) {
                groupFee = BigDecimal.ZERO;
            } else {
                BigDecimal orderSubtotal = lines.stream().map(PreparedLine::itemTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                groupFee = totalShippingFee.multiply(groupSubtotal)
                        .divide(orderSubtotal, 0, java.math.RoundingMode.HALF_UP);
            }
            allocatedSum = allocatedSum.add(groupFee);

            List<BigDecimal> itemTotals = group.lines().stream().map(PreparedLine::itemTotal).toList();
            List<BigDecimal> allocations = ShippingFeeAllocator.allocateProportionally(groupFee, itemTotals);
            for (int i = 0; i < group.lines().size(); i++) {
                group.lines().get(i).setShippingFeeAllocated(allocations.get(i));
            }
        }
    }

    private Map<UUID, SellerGroup> groupLinesBySeller(List<PreparedLine> lines) {
        Map<UUID, SellerGroup> groups = new LinkedHashMap<>();
        for (PreparedLine line : lines) {
            groups.computeIfAbsent(line.sellerId(), ignored -> new SellerGroup()).add(line);
        }
        return groups;
    }

    private Map<UUID, Integer> aggregateQuantityByProduct(List<PreparedLine> lines) {
        Map<UUID, Integer> quantityByProduct = new LinkedHashMap<>();
        for (PreparedLine line : lines) {
            quantityByProduct.merge(line.productId(), line.quantity(), Integer::sum);
        }
        return quantityByProduct;
    }

    private void lockInventories(List<UUID> productIds) {
        if (productIds.isEmpty()) {
            return;
        }
        String sql = """
                SELECT product_id
                FROM product_inventories
                WHERE product_id IN (:productIds)
                ORDER BY product_id
                FOR UPDATE
                """;
        jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productIds", productIds),
                (rs, rowNum) -> rs.getString("product_id")
        );
    }

    private void reserveInventoryOrThrow(Map<UUID, Integer> quantityByProduct, Instant now) {
        String sql = """
                UPDATE product_inventories
                SET stock_quantity = stock_quantity - :quantity,
                    reserved_quantity = reserved_quantity + :quantity,
                    updated_at = :now
                WHERE product_id = :productId
                  AND stock_quantity >= :quantity
                """;
        for (Map.Entry<UUID, Integer> entry : quantityByProduct.entrySet()) {
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("productId", entry.getKey())
                    .addValue("quantity", entry.getValue())
                    .addValue("now", Timestamp.from(now)));
            if (updated == 0) {
                log.warn("Stock conflict during checkout for product {}", entry.getKey());
                throw new AppException(ErrorCode.OUT_OF_STOCK, "Insufficient stock for checkout");
            }
        }
    }

    private OrderStatus resolveOrderStatus(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.PAYOS ? OrderStatus.AWAITING_PAYMENT : OrderStatus.PROCESSING;
    }

    private void insertOrder(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalAmount,
            BigDecimal finalAmount,
            PaymentMethod paymentMethod,
            OrderStatus orderStatus,
            Instant now
    ) {
        String sql = """
                INSERT INTO orders(
                    id, buyer_id, total_amount, final_amount, payment_method,
                    status, payment_status, created_at, updated_at
                ) VALUES (
                    :orderId, :buyerId, :totalAmount, :finalAmount, CAST(:paymentMethod AS payment_method),
                    CAST(:status AS order_status), 'PENDING', :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("buyerId", buyerId)
                .addValue("totalAmount", totalAmount)
                .addValue("finalAmount", finalAmount)
                .addValue("paymentMethod", paymentMethod.name())
                .addValue("status", orderStatus.name())
                .addValue("now", Timestamp.from(now)));
    }

    private void insertOrderItems(UUID orderId, List<PreparedLine> lines, Instant now) {
        String sql = """
                INSERT INTO order_items(
                    id, order_id, product_id, seller_id, quantity,
                    unit_price_snapshot, final_price, sku_snapshot, product_name_snapshot,
                    image_snapshot, attributes_snapshot, shipping_fee_allocated, shop_name_snapshot,
                    status, created_at, updated_at
                ) VALUES (
                    :id, :orderId, :productId, :sellerId, :quantity,
                    :unitPrice, :finalPrice, :sku, :productName,
                    :imageUrl, CAST(:attributesJson AS jsonb), :shippingFeeAllocated, :shopName,
                    'PENDING', :now, :now
                )
                """;
        for (PreparedLine line : lines) {
            jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID())
                    .addValue("orderId", orderId)
                    .addValue("productId", line.productId())
                    .addValue("sellerId", line.sellerId())
                    .addValue("quantity", line.quantity())
                    .addValue("unitPrice", line.unitPrice())
                    .addValue("finalPrice", line.itemTotal())
                    .addValue("sku", line.sku())
                    .addValue("productName", line.productName())
                    .addValue("imageUrl", line.imageUrl())
                    .addValue("attributesJson", line.attributesJson() == null ? "{}" : line.attributesJson())
                    .addValue("shippingFeeAllocated", line.shippingFeeAllocated())
                    .addValue("shopName", line.shopName())
                    .addValue("now", Timestamp.from(now)));
        }
    }

    private void insertPayment(
            UUID paymentId,
            UUID orderId,
            UUID payerId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String idempotencyKey,
            Instant now
    ) {
        Instant expiredAt = paymentMethod == PaymentMethod.PAYOS
                ? now.plusSeconds(paymentTtlMinutes * 60L)
                : null;

        String sql = """
                INSERT INTO payments(
                    id, order_id, payer_id, amount, currency, payment_method, status,
                    idempotency_key, expired_at, checkout_url_expired_at, created_at, updated_at
                ) VALUES (
                    :paymentId, :orderId, :payerId, :amount, 'VND', CAST(:paymentMethod AS payment_method), 'PENDING',
                    :idempotencyKey, :expiredAt, :checkoutUrlExpiredAt, :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("orderId", orderId)
                .addValue("payerId", payerId)
                .addValue("amount", amount)
                .addValue("paymentMethod", paymentMethod.name())
                .addValue("idempotencyKey", idempotencyKey)
                .addValue("expiredAt", expiredAt == null ? null : Timestamp.from(expiredAt))
                .addValue("checkoutUrlExpiredAt", expiredAt == null ? null : Timestamp.from(expiredAt))
                .addValue("now", Timestamp.from(now)));
    }

    private void insertOrderStatusHistory(UUID orderId, String oldStatus, String newStatus, Instant now) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (
                    :id, :orderId,
                    CAST(:oldStatus AS order_status),
                    CAST(:newStatus AS order_status),
                    :changedBy, :note, :createdAt
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("newStatus", newStatus)
                .addValue("changedBy", CHANGED_BY)
                .addValue("note", CHECKOUT_NOTE)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, String newStatus, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (
                    :id, :paymentId,
                    CAST(:oldStatus AS payment_status),
                    CAST(:newStatus AS payment_status),
                    CAST(:payload AS jsonb),
                    :createdAt
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("newStatus", newStatus)
                .addValue("payload", buildPaymentHistoryPayload(now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", CHECKOUT_NOTE);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private String resolvePayosCheckoutUrl(PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.PAYOS) {
            return null;
        }
        return null;
    }

    private ProductCheckoutRow mapProductCheckoutRow(ResultSet rs) throws SQLException {
        return new ProductCheckoutRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("title"),
                rs.getInt("weight_gram"),
                rs.getString("product_status"),
                rs.getString("shop_name"),
                rs.getString("shop_status"),
                rs.getBoolean("is_vacation"),
                rs.getBoolean("category_active"),
                rs.getInt("stock_quantity"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("sale_price"),
                rs.getString("image_url"),
                rs.getString("sku"),
                rs.getString("attributes_json")
        );
    }

    private record ProductCheckoutRow(
            UUID productId,
            UUID sellerId,
            UUID shopId,
            String title,
            int weightGram,
            String productStatus,
            String shopName,
            String shopStatus,
            boolean isVacation,
            boolean categoryActive,
            int stockQuantity,
            BigDecimal price,
            BigDecimal salePrice,
            String imageUrl,
            String sku,
            String attributesJson
    ) {
    }

    private static final class PreparedLine {
        private final UUID cartItemId;
        private final UUID productId;
        private final UUID sellerId;
        private final UUID shopId;
        private final String productName;
        private final String shopName;
        private final String sku;
        private final String imageUrl;
        private final String attributesJson;
        private final BigDecimal unitPrice;
        private final int quantity;
        private final BigDecimal itemTotal;
        private final int weightGram;
        private BigDecimal shippingFeeAllocated;

        private PreparedLine(
                UUID cartItemId,
                UUID productId,
                UUID sellerId,
                UUID shopId,
                String productName,
                String shopName,
                String sku,
                String imageUrl,
                String attributesJson,
                BigDecimal unitPrice,
                int quantity,
                BigDecimal itemTotal,
                int weightGram,
                BigDecimal shippingFeeAllocated
        ) {
            this.cartItemId = cartItemId;
            this.productId = productId;
            this.sellerId = sellerId;
            this.shopId = shopId;
            this.productName = productName;
            this.shopName = shopName;
            this.sku = sku;
            this.imageUrl = imageUrl;
            this.attributesJson = attributesJson;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.itemTotal = itemTotal;
            this.weightGram = weightGram;
            this.shippingFeeAllocated = shippingFeeAllocated;
        }

        private UUID sellerId() {
            return sellerId;
        }

        private UUID shopId() {
            return shopId;
        }

        private UUID productId() {
            return productId;
        }

        private int quantity() {
            return quantity;
        }

        private BigDecimal unitPrice() {
            return unitPrice;
        }

        private BigDecimal itemTotal() {
            return itemTotal;
        }

        private int weightGram() {
            return weightGram;
        }

        private String productName() {
            return productName;
        }

        private String shopName() {
            return shopName;
        }

        private String sku() {
            return sku;
        }

        private String imageUrl() {
            return imageUrl;
        }

        private String attributesJson() {
            return attributesJson;
        }

        private BigDecimal shippingFeeAllocated() {
            return shippingFeeAllocated;
        }

        private void setShippingFeeAllocated(BigDecimal shippingFeeAllocated) {
            this.shippingFeeAllocated = shippingFeeAllocated;
        }
    }

    private static final class SellerGroup {
        private final List<PreparedLine> lines = new ArrayList<>();

        private void add(PreparedLine line) {
            lines.add(line);
        }

        private UUID shopId() {
            return lines.getFirst().shopId();
        }

        private int totalWeightGram() {
            return lines.stream().mapToInt(PreparedLine::weightGram).sum();
        }

        private List<PreparedLine> lines() {
            return lines;
        }
    }
}
