package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.BuyerDeliveryAddress;
import com.twohands.commerce_service.domain.shipment.CreateShipmentDraft;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
import com.twohands.commerce_service.domain.shipment.CreateShipmentRepository;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import com.twohands.commerce_service.domain.shipment.SellerPickupAddress;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentCreatedBySource;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemLine;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CreateShipmentRepositoryAdapter implements CreateShipmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateShipmentRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CreateShipmentOrderContext> findOrderContext(UUID orderId) {
        String sql = """
                SELECT id,
                       buyer_id,
                       status::text AS order_status,
                       payment_method::text AS payment_method,
                       payment_status::text AS payment_status,
                       final_amount
                FROM orders
                WHERE id = :orderId
                """;
        List<CreateShipmentOrderContext> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new CreateShipmentOrderContext(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("buyer_id")),
                        rs.getString("order_status"),
                        PaymentMethod.valueOf(rs.getString("payment_method")),
                        PaymentStatus.valueOf(rs.getString("payment_status")),
                        rs.getBigDecimal("final_amount")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<BuyerDeliveryAddress> findBuyerDeliveryAddress(UUID buyerId) {
        String defaultSql = """
                SELECT id, receiver_name, phone, province_code, district_code, ward_code, address_detail
                FROM user_addresses
                WHERE user_id = :buyerId AND is_default = TRUE
                LIMIT 1
                """;
        List<BuyerDeliveryAddress> rows = jdbcTemplate.query(
                defaultSql,
                new MapSqlParameterSource("buyerId", buyerId),
                this::mapBuyerAddress
        );
        if (!rows.isEmpty()) {
            return Optional.of(rows.getFirst());
        }

        String fallbackSql = """
                SELECT id, receiver_name, phone, province_code, district_code, ward_code, address_detail
                FROM user_addresses
                WHERE user_id = :buyerId
                ORDER BY updated_at DESC
                LIMIT 1
                """;
        rows = jdbcTemplate.query(fallbackSql, new MapSqlParameterSource("buyerId", buyerId), this::mapBuyerAddress);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<SellerPickupAddress> findSellerPickupBySellerId(UUID sellerId) {
        String sql = """
                SELECT ssp.shop_id,
                       ss.seller_id,
                       ssp.pickup_name,
                       ssp.phone,
                       ssp.province_code,
                       ssp.district_code,
                       ssp.ward_code,
                       ssp.address_detail
                FROM seller_shipping_profiles ssp
                INNER JOIN seller_shops ss ON ss.id = ssp.shop_id
                WHERE ss.seller_id = :sellerId
                LIMIT 1
                """;
        List<SellerPickupAddress> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                (rs, rowNum) -> new SellerPickupAddress(
                        UUID.fromString(rs.getString("shop_id")),
                        UUID.fromString(rs.getString("seller_id")),
                        rs.getString("pickup_name"),
                        rs.getString("phone"),
                        rs.getString("province_code"),
                        rs.getString("district_code"),
                        rs.getString("ward_code"),
                        rs.getString("address_detail")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public List<ShipmentOrderItemLine> findOrderItemsForSeller(
            UUID orderId,
            UUID sellerId,
            List<UUID> orderItemIds
    ) {
        String sql = """
                SELECT oi.id AS order_item_id,
                       oi.product_id,
                       oi.seller_id,
                       oi.status::text AS item_status,
                       oi.shipment_id,
                       oi.quantity,
                       oi.final_price,
                       oi.shipping_fee_allocated,
                       (p.weight_gram * oi.quantity) AS weight_gram
                FROM order_items oi
                INNER JOIN products p ON p.id = oi.product_id
                WHERE oi.order_id = :orderId
                  AND oi.seller_id = :sellerId
                  AND oi.id IN (:orderItemIds)
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("sellerId", sellerId)
                .addValue("orderItemIds", orderItemIds), this::mapOrderItemLine);
    }

    @Override
    public CreateShipmentResult createShipment(CreateShipmentDraft draft, Instant occurredAt) {
        try {
            insertShipment(draft, occurredAt);
            insertAddressSnapshot(draft, occurredAt);
            attachOrderItems(draft, occurredAt);
            insertStatusHistory(draft.shipmentId(), occurredAt);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.ORDER_ITEM_ALREADY_SHIPPED, "Order item already shipped", ex);
        }

        return new CreateShipmentResult(
                draft.shipmentId(),
                draft.orderId(),
                draft.sellerId(),
                draft.carrier(),
                draft.shipmentType(),
                ShipmentStatus.PENDING,
                null,
                draft.trackingNumber(),
                draft.shippingFee(),
                draft.codAmount(),
                draft.totalWeightGram(),
                draft.estimatedDeliveryDate(),
                draft.orderItemIds(),
                occurredAt
        );
    }

    @Override
    public void updateGhnProviderFields(
            UUID shipmentId,
            GhnCreateOrderResult ghnResult,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE shipments
                SET ghn_order_code = :ghnOrderCode,
                    ghn_shop_id = :ghnShopId,
                    tracking_number = COALESCE(:trackingNumber, tracking_number),
                    external_provider_response = CAST(:providerResponse AS jsonb),
                    updated_at = :now
                WHERE id = :shipmentId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("ghnOrderCode", ghnResult.ghnOrderCode())
                .addValue("ghnShopId", ghnResult.ghnShopId())
                .addValue("trackingNumber", ghnResult.trackingNumber())
                .addValue("providerResponse", ghnResult.providerResponseJson())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void insertShipment(CreateShipmentDraft draft, Instant occurredAt) {
        String sql = """
                INSERT INTO shipments(
                    id, order_id, seller_id, carrier, shipment_type, weight_gram,
                    shipping_fee, cod_amount, estimated_delivery_date, tracking_number,
                    status, created_by_source, created_at, updated_at
                ) VALUES (
                    :shipmentId, :orderId, :sellerId, CAST(:carrier AS shipment_carrier),
                    CAST(:shipmentType AS shipment_type), :weightGram,
                    :shippingFee, :codAmount, :estimatedDeliveryDate, :trackingNumber,
                    CAST(:status AS shipment_status), CAST(:createdBy AS shipment_created_by_source),
                    :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", draft.shipmentId())
                .addValue("orderId", draft.orderId())
                .addValue("sellerId", draft.sellerId())
                .addValue("carrier", draft.carrier().name())
                .addValue("shipmentType", draft.shipmentType().name())
                .addValue("weightGram", draft.totalWeightGram())
                .addValue("shippingFee", draft.shippingFee())
                .addValue("codAmount", draft.codAmount())
                .addValue("estimatedDeliveryDate", draft.estimatedDeliveryDate())
                .addValue("trackingNumber", draft.trackingNumber())
                .addValue("status", ShipmentStatus.PENDING.name())
                .addValue("createdBy", ShipmentCreatedBySource.SELLER.name())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void insertAddressSnapshot(CreateShipmentDraft draft, Instant occurredAt) {
        BuyerDeliveryAddress address = draft.deliveryAddress();
        String sql = """
                INSERT INTO shipping_address_snapshots(
                    id, shipment_id, receiver_name, phone, province_code, district_code,
                    ward_code, address_detail, full_address, created_at
                ) VALUES (
                    :id, :shipmentId, :receiverName, :phone, :provinceCode, :districtCode,
                    :wardCode, :addressDetail, :fullAddress, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("shipmentId", draft.shipmentId())
                .addValue("receiverName", address.receiverName())
                .addValue("phone", address.phone())
                .addValue("provinceCode", address.provinceCode())
                .addValue("districtCode", address.districtCode())
                .addValue("wardCode", address.wardCode())
                .addValue("addressDetail", address.addressDetail())
                .addValue("fullAddress", address.fullAddress())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void attachOrderItems(CreateShipmentDraft draft, Instant occurredAt) {
        String sql = """
                UPDATE order_items
                SET shipment_id = :shipmentId,
                    updated_at = :now
                WHERE id IN (:orderItemIds)
                  AND order_id = :orderId
                  AND seller_id = :sellerId
                  AND shipment_id IS NULL
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", draft.shipmentId())
                .addValue("orderItemIds", draft.orderItemIds())
                .addValue("orderId", draft.orderId())
                .addValue("sellerId", draft.sellerId())
                .addValue("now", Timestamp.from(occurredAt)));
        if (updated != draft.orderItemIds().size()) {
            throw new AppException(ErrorCode.ORDER_ITEM_ALREADY_SHIPPED);
        }
    }

    private void insertStatusHistory(UUID shipmentId, Instant occurredAt) {
        String sql = """
                INSERT INTO shipment_status_history(
                    id, shipment_id, old_status, new_status, created_at
                ) VALUES (
                    :id, :shipmentId, NULL, CAST(:newStatus AS shipment_status), :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("shipmentId", shipmentId)
                .addValue("newStatus", ShipmentStatus.PENDING.name())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private BuyerDeliveryAddress mapBuyerAddress(ResultSet rs, int rowNum) throws SQLException {
        return new BuyerDeliveryAddress(
                UUID.fromString(rs.getString("id")),
                rs.getString("receiver_name"),
                rs.getString("phone"),
                rs.getString("province_code"),
                rs.getString("district_code"),
                rs.getString("ward_code"),
                rs.getString("address_detail")
        );
    }

    private ShipmentOrderItemLine mapOrderItemLine(ResultSet rs, int rowNum) throws SQLException {
        String shipmentId = rs.getString("shipment_id");
        return new ShipmentOrderItemLine(
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("product_id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("item_status"),
                shipmentId != null ? UUID.fromString(shipmentId) : null,
                rs.getInt("quantity"),
                rs.getBigDecimal("final_price"),
                rs.getBigDecimal("shipping_fee_allocated"),
                rs.getInt("weight_gram")
        );
    }
}
