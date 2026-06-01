const reviewIdByOrderItemId = new Map();

export function getReviewIdForOrderItem(orderItemId) {
  return reviewIdByOrderItemId.get(orderItemId) || null;
}

export function setReviewIdForOrderItem(orderItemId, reviewId) {
  reviewIdByOrderItemId.set(orderItemId, reviewId);
}

export function hasReviewForOrderItem(orderItemId) {
  return reviewIdByOrderItemId.has(orderItemId);
}

export function getReviewIndexEntries() {
  return reviewIdByOrderItemId;
}
