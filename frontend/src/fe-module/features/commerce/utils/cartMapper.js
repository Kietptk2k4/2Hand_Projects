function pick(obj, snakeKey, camelKey) {
  return obj?.[snakeKey] ?? obj?.[camelKey];
}

function mapCartProductSnapshot(product) {
  if (!product) return null;

  return {
    productId: pick(product, "product_id", "productId"),
    sellerId: pick(product, "seller_id", "sellerId"),
    shopId: pick(product, "shop_id", "shopId"),
    productName: pick(product, "product_name", "productName"),
    imageUrl: pick(product, "image_url", "imageUrl"),
    price: product.price,
    salePrice: pick(product, "sale_price", "salePrice"),
    effectivePrice: pick(product, "effective_price", "effectivePrice"),
    inStock: pick(product, "in_stock", "inStock"),
    availableQuantity: pick(product, "available_quantity", "availableQuantity"),
  };
}

export function mapAddProductToCartResponse(data) {
  if (!data) return null;

  return {
    cartId: pick(data, "cart_id", "cartId"),
    cartItemId: pick(data, "cart_item_id", "cartItemId"),
    productId: pick(data, "product_id", "productId"),
    quantity: data.quantity,
    status: data.status,
    product: mapCartProductSnapshot(data.product),
  };
}

export function mapValidateCartItemsResponse(data) {
  if (!data) return null;

  const mapEntry = (entry) => ({
    cartItemId: pick(entry, "cart_item_id", "cartItemId"),
    reason: entry.reason,
    currentStatus: pick(entry, "current_status", "currentStatus"),
  });

  return {
    validItems: (data.valid_items ?? data.validItems ?? []).map(mapEntry),
    invalidItems: (data.invalid_items ?? data.invalidItems ?? []).map(mapEntry),
    canCheckout: Boolean(data.can_checkout ?? data.canCheckout),
  };
}

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
