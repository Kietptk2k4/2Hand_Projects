export function mapProductTagsFromApi(tags = []) {
  return (tags || [])
    .filter((tag) => tag?.productId)
    .map((tag) => ({
      productId: tag.productId,
      price: Number(tag.price) >= 0 ? Number(tag.price) : 0,
      name: tag.name || "Sản phẩm",
      category: tag.category || "",
      imageUrl: tag.imageUrl || null,
      available: tag.available !== false,
    }));
}
