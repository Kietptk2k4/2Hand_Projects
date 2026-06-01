export function mapProductDetailResponse(data) {
  if (!data) return null;

  const media = (data.media || [])
    .map((item) => ({
      mediaId: item.media_id,
      mediaUrl: item.media_url,
      mediaType: item.media_type,
      sortOrder: item.sort_order ?? 0,
    }))
    .sort((a, b) => a.sortOrder - b.sortOrder);

  const attributes = (data.attributes || []).map((item) => ({
    attributeName: item.attribute_name,
    attributeValue: item.attribute_value,
  }));

  const inventory = data.inventory_summary || {};

  return {
    productId: data.product_id,
    title: data.title,
    description: data.description,
    condition: data.condition,
    weightGram: data.weight_gram,
    status: data.status,
    category: data.category
      ? {
          categoryId: data.category.category_id,
          name: data.category.name,
          slug: data.category.slug,
        }
      : null,
    shop: data.shop
      ? {
          shopId: data.shop.shop_id,
          shopName: data.shop.shop_name,
          avatarUrl: data.shop.avatar_url,
          coverUrl: data.shop.cover_url,
        }
      : null,
    media,
    attributes,
    price: data.price,
    salePrice: data.sale_price,
    effectivePrice: data.effective_price,
    inventorySummary: {
      stockQuantity: inventory.stock_quantity,
      lowStockThreshold: inventory.low_stock_threshold,
      inStock: inventory.in_stock,
      lowStock: inventory.low_stock,
    },
    ratingAvg: data.rating_avg,
    ratingCount: data.rating_count,
    shopVacation: data.shop_vacation,
    vacationMessage: data.vacation_message,
  };
}
