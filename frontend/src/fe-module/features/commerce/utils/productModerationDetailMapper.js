function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapProductModerationDetail(data) {
  if (!data) return null;

  return {
    productId: pick(data, "productId", "product_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopId: pick(data, "shopId", "shop_id"),
    shopName: pick(data, "shopName", "shop_name"),
    title: data.title,
    description: data.description,
    status: data.status,
    categoryId: pick(data, "categoryId", "category_id"),
    categoryName: pick(data, "categoryName", "category_name"),
    price: data.price,
    effectivePrice: pick(data, "effectivePrice", "effective_price") ?? data.price,
    stockQuantity: pick(data, "stockQuantity", "stock_quantity") ?? 0,
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    removedAt: pick(data, "removedAt", "removed_at"),
    removeReason: pick(data, "removeReason", "remove_reason"),
    openOrderCount: pick(data, "openOrderCount", "open_order_count") ?? 0,
    media: (data.media || []).map((item) => ({
      mediaUrl: pick(item, "mediaUrl", "media_url"),
      mediaType: pick(item, "mediaType", "media_type"),
      sortOrder: pick(item, "sortOrder", "sort_order") ?? 0,
    })),
    attributes: (data.attributes || []).map((item) => ({
      name: pick(item, "name", "attribute_name") ?? item.attribute_name,
      value: pick(item, "value", "attribute_value") ?? item.attribute_value,
    })),
    thumbnailUrl:
      pick(data, "thumbnailUrl", "thumbnail_url") ||
      data.media?.[0]?.media_url ||
      data.media?.[0]?.mediaUrl ||
      null,
  };
}
