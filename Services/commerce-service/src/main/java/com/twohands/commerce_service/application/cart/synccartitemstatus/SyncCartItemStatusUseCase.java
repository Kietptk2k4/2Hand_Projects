package com.twohands.commerce_service.application.cart.synccartitemstatus;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemSyncCandidate;
import com.twohands.commerce_service.domain.cart.CartItemSyncEvaluator;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusRepository;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SyncCartItemStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncCartItemStatusUseCase.class);

    private final SyncCartItemStatusRepository syncCartItemStatusRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;
    private final Clock clock;
    private final int batchSize;

    public SyncCartItemStatusUseCase(
            SyncCartItemStatusRepository syncCartItemStatusRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository,
            Clock clock,
            @Value("${commerce.jobs.sync-cart-item-status.batch-size:100}") int batchSize
    ) {
        this.syncCartItemStatusRepository = syncCartItemStatusRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Transactional
    public SyncCartItemStatusResult syncForUser(UUID userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            return SyncCartItemStatusResult.empty();
        }
        List<CartItemSyncCandidate> candidates = syncCartItemStatusRepository.findCandidatesByCartId(cart.get().id());
        return syncCandidates(candidates, true);
    }

    @Transactional
    public SyncCartItemStatusResult syncByProductId(UUID productId) {
        List<CartItemSyncCandidate> candidates = syncCartItemStatusRepository.findCandidatesByProductId(productId);
        return syncCandidates(candidates, true);
    }

    @Transactional
    public SyncCartItemStatusResult syncBySellerId(UUID sellerId) {
        List<CartItemSyncCandidate> candidates = syncCartItemStatusRepository.findCandidatesBySellerId(sellerId);
        return syncCandidates(candidates, true);
    }

    @Transactional
    public SyncCartItemStatusResult syncBatch() {
        List<CartItemSyncCandidate> candidates = syncCartItemStatusRepository.findCandidateBatch(batchSize);
        return syncCandidates(candidates, false);
    }

    private SyncCartItemStatusResult syncCandidates(
            List<CartItemSyncCandidate> candidates,
            boolean allowInvalidRestore
    ) {
        if (candidates.isEmpty()) {
            return SyncCartItemStatusResult.empty();
        }

        Instant now = clock.instant();
        Set<UUID> productIds = candidates.stream()
                .map(CartItemSyncCandidate::productId)
                .collect(Collectors.toSet());
        Map<UUID, ProductPurchaseContext> contexts = productPurchaseReadRepository.findByProductIds(productIds);

        int updated = 0;
        int unchanged = 0;
        int skipped = 0;

        for (CartItemSyncCandidate candidate : candidates) {
            ProductPurchaseContext context = contexts.get(candidate.productId());
            Optional<CartItemStatus> targetStatus = CartItemSyncEvaluator.resolveTargetStatus(
                    candidate.currentStatus(),
                    context,
                    candidate.quantity(),
                    allowInvalidRestore
            );

            if (targetStatus.isEmpty()) {
                skipped++;
                continue;
            }

            if (targetStatus.get() == candidate.currentStatus()) {
                unchanged++;
                continue;
            }

            CartItem item = cartItemRepository.findByIdAndCartId(candidate.cartItemId(), candidate.cartId())
                    .orElse(null);
            if (item == null) {
                skipped++;
                continue;
            }

            cartItemRepository.save(item.withStatus(targetStatus.get(), now));
            updated++;
        }

        if (updated > 0) {
            log.debug(
                    "Cart item status sync completed. scanned={}, updated={}, unchanged={}, skipped={}",
                    candidates.size(),
                    updated,
                    unchanged,
                    skipped
            );
        }

        return new SyncCartItemStatusResult(candidates.size(), updated, unchanged, skipped);
    }
}
