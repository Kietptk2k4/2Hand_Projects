function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapAdminProductListItem(item) {
  if (!item) return null;

  return {
    productId: pick(item, "productId", "product_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    shopId: pick(item, "shopId", "shop_id"),
    shopName: pick(item, "shopName", "shop_name"),
    title: item.title,
    thumbnailUrl: pick(item, "thumbnailUrl", "thumbnail_url"),
    categoryId: pick(item, "categoryId", "category_id"),
    categoryName: pick(item, "categoryName", "category_name"),
    price: item.price,
    effectivePrice: item.effective_price ?? item.effectivePrice ?? item.price,
    status: item.status,
    createdAt: pick(item, "createdAt", "created_at"),
    removedAt: pick(item, "removedAt", "removed_at"),
    removeReason: pick(item, "removeReason", "remove_reason"),
  };
}

export function mapAdminProductListResponse(data) {
  if (!data) {
    return { items: [], pagination: null };
  }

  return {
    items: (data.items || []).map(mapAdminProductListItem).filter(Boolean),
    pagination: data.pagination
      ? {
          page: data.pagination.page,
          limit: data.pagination.limit,
          totalItems: data.pagination.total_items ?? data.pagination.totalItems,
          totalPages: data.pagination.total_pages ?? data.pagination.totalPages,
          hasNext: Boolean(data.pagination.has_next ?? data.pagination.hasNext),
        }
      : null,
  };
}

export function mapRemoveProductPayload({ reason }) {
  return {
    reason: String(reason ?? "").trim(),
  };
}

export function mapRemoveProductResponse(data) {
  if (!data) return null;

  return {
    productId: pick(data, "productId", "product_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopId: pick(data, "shopId", "shop_id"),
    title: data.title,
    status: data.status,
    previousStatus: pick(data, "previousStatus", "previous_status"),
    alreadyRemoved: Boolean(data.already_removed ?? data.alreadyRemoved),
    cartItemsInvalidated: data.cart_items_invalidated ?? data.cartItemsInvalidated ?? 0,
    removedAt: pick(data, "removedAt", "removed_at"),
  };
}
