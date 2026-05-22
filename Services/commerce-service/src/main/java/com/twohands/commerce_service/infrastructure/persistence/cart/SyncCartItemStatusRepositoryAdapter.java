package com.twohands.commerce_service.infrastructure.persistence.cart;

import com.twohands.commerce_service.domain.cart.CartItemSyncCandidate;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartItemEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.CartItemStatusType;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.CartItemJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Repository
public class SyncCartItemStatusRepositoryAdapter implements SyncCartItemStatusRepository {

    private static final EnumSet<CartItemStatusType> CART_VIEW_SYNC_STATUSES = EnumSet.of(
            CartItemStatusType.ACTIVE,
            CartItemStatusType.OUT_OF_STOCK,
            CartItemStatusType.INVALID_PRODUCT
    );

    private static final EnumSet<CartItemStatusType> SCOPED_SYNC_STATUSES = EnumSet.of(
            CartItemStatusType.ACTIVE,
            CartItemStatusType.OUT_OF_STOCK,
            CartItemStatusType.INVALID_PRODUCT
    );

    private static final EnumSet<CartItemStatusType> BATCH_SYNC_STATUSES = EnumSet.of(
            CartItemStatusType.ACTIVE,
            CartItemStatusType.OUT_OF_STOCK
    );

    private final CartItemJpaRepository cartItemJpaRepository;

    public SyncCartItemStatusRepositoryAdapter(CartItemJpaRepository cartItemJpaRepository) {
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Override
    public List<CartItemSyncCandidate> findCandidatesByCartId(UUID cartId) {
        return cartItemJpaRepository.findByCartIdAndStatusIn(cartId, CART_VIEW_SYNC_STATUSES).stream()
                .map(this::toCandidate)
                .toList();
    }

    @Override
    public List<CartItemSyncCandidate> findCandidatesByProductId(UUID productId) {
        return cartItemJpaRepository.findByProductIdAndStatusIn(productId, SCOPED_SYNC_STATUSES).stream()
                .map(this::toCandidate)
                .toList();
    }

    @Override
    public List<CartItemSyncCandidate> findCandidatesBySellerId(UUID sellerId) {
        return cartItemJpaRepository.findBySellerIdAndStatusIn(sellerId, SCOPED_SYNC_STATUSES).stream()
                .map(this::toCandidate)
                .toList();
    }

    @Override
    public List<CartItemSyncCandidate> findCandidateBatch(int limit) {
        return cartItemJpaRepository.findSyncBatch(BATCH_SYNC_STATUSES, PageRequest.of(0, limit)).stream()
                .map(this::toCandidate)
                .toList();
    }

    private CartItemSyncCandidate toCandidate(CartItemEntity entity) {
        return new CartItemSyncCandidate(
                entity.getId(),
                entity.getCartId(),
                entity.getProductId(),
                entity.getSellerId(),
                entity.getQuantity(),
                PersistenceEnumMapper.toDomain(entity.getStatus())
        );
    }
}
