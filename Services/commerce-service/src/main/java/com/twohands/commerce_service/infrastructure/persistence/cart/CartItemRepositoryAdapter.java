package com.twohands.commerce_service.infrastructure.persistence.cart;

import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartItemEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.CartItemJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.CartItemStatusType;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumSet;
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
    public int markInvalidByProductId(UUID productId, Instant updatedAt) {
        return cartItemJpaRepository.markInvalidByProductId(
                productId,
                CartItemStatusType.INVALID_PRODUCT,
                EnumSet.of(CartItemStatusType.ACTIVE, CartItemStatusType.OUT_OF_STOCK),
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
