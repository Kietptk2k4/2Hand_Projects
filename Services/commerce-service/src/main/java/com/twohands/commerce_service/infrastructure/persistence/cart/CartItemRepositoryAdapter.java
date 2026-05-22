package com.twohands.commerce_service.infrastructure.persistence.cart;

import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartItemEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.CartItemJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.CartItemStatusType;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CartItemRepositoryAdapter implements CartItemRepository {

    private final CartItemJpaRepository cartItemJpaRepository;

    public CartItemRepositoryAdapter(CartItemJpaRepository cartItemJpaRepository) {
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Override
    public Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId) {
        return cartItemJpaRepository.findByCartIdAndProductId(cartId, productId).map(this::toDomain);
    }

    @Override
    public Optional<CartItem> findByIdAndCartId(UUID cartItemId, UUID cartId) {
        return cartItemJpaRepository.findByIdAndCartId(cartItemId, cartId).map(this::toDomain);
    }

    @Override
    public List<CartItem> findByCartIdExcludingRemoved(UUID cartId) {
        return cartItemJpaRepository.findByCartIdAndStatusNot(cartId, CartItemStatusType.REMOVED).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CartItem> findByCartIdAndIds(UUID cartId, Collection<UUID> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return List.of();
        }
        return cartItemJpaRepository.findByCartIdAndIdIn(cartId, cartItemIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int markInvalidByProductId(UUID productId, Instant updatedAt) {
        return markInvalid(productId, null, updatedAt);
    }

    @Override
    public int markInvalidBySellerId(UUID sellerId, Instant updatedAt) {
        return markInvalid(null, sellerId, updatedAt);
    }

    private int markInvalid(UUID productId, UUID sellerId, Instant updatedAt) {
        EnumSet<CartItemStatusType> eligible = EnumSet.of(
                CartItemStatusType.ACTIVE,
                CartItemStatusType.OUT_OF_STOCK
        );
        if (productId != null) {
            return cartItemJpaRepository.markInvalidByProductId(
                    productId,
                    CartItemStatusType.INVALID_PRODUCT,
                    eligible,
                    updatedAt
            );
        }
        return cartItemJpaRepository.markInvalidBySellerId(
                sellerId,
                CartItemStatusType.INVALID_PRODUCT,
                eligible,
                updatedAt
        );
    }

    @Override
    public CartItem save(CartItem cartItem) {
        CartItemEntity entity;
        if (cartItem.id() != null) {
            entity = cartItemJpaRepository.findById(cartItem.id()).orElseGet(CartItemEntity::new);
            entity.setId(cartItem.id());
        } else {
            entity = new CartItemEntity();
        }
        entity.setCartId(cartItem.cartId());
        entity.setProductId(cartItem.productId());
        entity.setSellerId(cartItem.sellerId());
        entity.setQuantity(cartItem.quantity());
        entity.setStatus(PersistenceEnumMapper.toEntity(cartItem.status()));
        entity.setCreatedAt(cartItem.createdAt());
        entity.setUpdatedAt(cartItem.updatedAt());
        return toDomain(cartItemJpaRepository.save(entity));
    }

    private CartItem toDomain(CartItemEntity entity) {
        return new CartItem(
                entity.getId(),
                entity.getCartId(),
                entity.getProductId(),
                entity.getSellerId(),
                entity.getQuantity(),
                PersistenceEnumMapper.toDomain(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
