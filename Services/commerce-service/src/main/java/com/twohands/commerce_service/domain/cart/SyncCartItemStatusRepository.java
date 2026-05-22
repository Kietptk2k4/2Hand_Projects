package com.twohands.commerce_service.domain.cart;

import java.util.List;
import java.util.UUID;

public interface SyncCartItemStatusRepository {

    List<CartItemSyncCandidate> findCandidatesByCartId(UUID cartId);

    List<CartItemSyncCandidate> findCandidatesByProductId(UUID productId);

    List<CartItemSyncCandidate> findCandidatesBySellerId(UUID sellerId);

    List<CartItemSyncCandidate> findCandidateBatch(int limit);
}
