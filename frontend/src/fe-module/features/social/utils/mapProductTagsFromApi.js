function pick(obj, ...keys) {
  for (const key of keys) {
    const value = obj?.[key];
    if (value != null && value !== "") return value;
  }
  return null;
}

export function mapProductTagsFromApi(tags = []) {
  return (tags || [])
    .filter((tag) => tag?.productId || tag?.product_id)
    .map((tag) => ({
      productId: pick(tag, "productId", "product_id"),
      price: Number(pick(tag, "price") ?? 0) >= 0 ? Number(pick(tag, "price") ?? 0) : 0,
      name: pick(tag, "name") || "Sản phẩm",
      category: pick(tag, "category") || "",
      categoryId: pick(tag, "categoryId", "category_id"),
      shopId: pick(tag, "shopId", "shop_id"),
      imageUrl: pick(tag, "imageUrl", "image_url") || null,
      available: tag.available !== false,
    }));
}
