function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapShopModerationDetail(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopName: pick(data, "shopName", "shop_name"),
    description: data.description || "",
    logoUrl: pick(data, "logoUrl", "logo_url"),
    status: data.status,
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    totalProductCount: pick(data, "totalProductCount", "total_product_count") ?? 0,
    activeProductCount: pick(data, "activeProductCount", "active_product_count") ?? 0,
    openOrderCount: pick(data, "openOrderCount", "open_order_count") ?? 0,
  };
}

export function mapShopModerationHistoryResponse(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    page: data.page,
    size: data.size,
    totalElements: pick(data, "totalElements", "total_elements") ?? 0,
    totalPages: pick(data, "totalPages", "total_pages") ?? 1,
    history: (data.history || []).map((entry) => ({
      moderationLogId: pick(entry, "moderationLogId", "moderation_log_id"),
      action: entry.action,
      reason: entry.reason,
      note: entry.note,
      adminId: pick(entry, "adminId", "admin_id"),
      createdAt: pick(entry, "createdAt", "created_at"),
    })),
  };
}
