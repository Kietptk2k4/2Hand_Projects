function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapShopModerationListItem(item) {
  if (!item) return null;

  return {
    shopId: pick(item, "shopId", "shop_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    shopName: pick(item, "shopName", "shop_name"),
    logoUrl: pick(item, "logoUrl", "logo_url"),
    status: item.status,
    createdAt: pick(item, "createdAt", "created_at"),
    updatedAt: pick(item, "updatedAt", "updated_at"),
    productCount: pick(item, "productCount", "product_count") ?? 0,
    activeProductCount: pick(item, "activeProductCount", "active_product_count") ?? 0,
  };
}

export function mapShopModerationListResponse(data) {
  if (!data) {
    return { items: [], pagination: null };
  }

  return {
    items: (data.items || []).map(mapShopModerationListItem).filter(Boolean),
    pagination: data.pagination
      ? {
          page: data.pagination.page,
          limit: data.pagination.limit,
          total_items: data.pagination.total_items ?? data.pagination.totalItems,
          total_pages: data.pagination.total_pages ?? data.pagination.totalPages,
          has_next: Boolean(data.pagination.has_next ?? data.pagination.hasNext),
        }
      : null,
  };
}
