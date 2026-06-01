export function mapCartResponse(data) {
  if (!data) return null;

  const items = (data.items || []).map((item) => ({
    cartItemId: item.cart_item_id,
    productId: item.product_id,
    sellerId: item.seller_id,
    shopId: item.shop_id,
    productName: item.product_name,
    imageUrl: item.image_url,
    quantity: item.quantity,
    status: item.status,
    effectivePrice: item.effective_price,
    inStock: item.in_stock,
    availableQuantity: item.available_quantity,
    unavailableReason: item.unavailable_reason,
  }));

  const summary = data.summary || {};

  return {
    cartId: data.cart_id,
    items,
    summary: {
      activeItemCount: summary.active_item_count ?? 0,
      invalidItemCount: summary.invalid_item_count ?? 0,
      subtotal: summary.subtotal ?? 0,
      canCheckout: Boolean(summary.can_checkout),
      warnings: summary.warnings || [],
    },
    createdAt: data.created_at,
    updatedAt: data.updated_at,
  };
}
