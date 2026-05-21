package com.twohands.commerce_service.infrastructure.persistence.cart;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.CartJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartJpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Cart getOrCreateByUserId(UUID userId) {
        return findByUserId(userId)
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    return save(new Cart(null, userId, now, now));
                });
    }

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.setId(cart.id() != null ? cart.id() : UUID.randomUUID());
        entity.setUserId(cart.userId());
        entity.setCreatedAt(cart.createdAt());
        entity.setUpdatedAt(cart.updatedAt());
        try {
            return toDomain(cartJpaRepository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            return cartJpaRepository.findByUserId(cart.userId())
                    .map(this::toDomain)
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    public void updateTimestamp(UUID cartId, Instant updatedAt) {
        cartJpaRepository.updateTimestamp(cartId, updatedAt);
    }

    private Cart toDomain(CartEntity entity) {
        return new Cart(entity.getId(), entity.getUserId(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
