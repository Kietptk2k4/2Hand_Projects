function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapAdminShopListItem(item) {
  if (!item) return null;

  return {
    shopId: pick(item, "shopId", "shop_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    shopName: pick(item, "shopName", "shop_name"),
    logoUrl: pick(item, "logoUrl", "logo_url"),
    status: item.status,
    createdAt: pick(item, "createdAt", "created_at"),
  };
}

export function mapAdminShopListResponse(data) {
  if (!data) {
    return { items: [], pagination: null };
  }

  return {
    items: (data.items || []).map(mapAdminShopListItem).filter(Boolean),
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

export function mapModerateShopPayload({ action, reason }) {
  return {
    action,
    reason: String(reason ?? "").trim(),
  };
}

export function mapModerateShopResponse(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopName: pick(data, "shopName", "shop_name"),
    status: data.status,
    previousStatus: pick(data, "previousStatus", "previous_status"),
    alreadyModerated: Boolean(data.already_moderated ?? data.alreadyModerated),
    cartItemsInvalidated: data.cart_items_invalidated ?? data.cartItemsInvalidated ?? 0,
    moderatedAt: pick(data, "moderatedAt", "moderated_at"),
  };
}
