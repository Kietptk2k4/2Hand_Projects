export function mapProductTagsFromApi(tags = []) {
  return (tags || [])
    .filter((tag) => tag?.productId || tag?.product_id)
    .map((tag) => ({
      productId: tag.productId || tag.product_id,
      price: Number(tag.price) >= 0 ? Number(tag.price) : 0,
      name: tag.name || tag.product_name || "Sản phẩm",
      category: tag.category || "",
      imageUrl: tag.imageUrl || tag.image_url || null,
      available: tag.available !== false,
    }));
}

export function resolvePostProductTags(post) {
  if (!post) return [];
  return mapProductTagsFromApi(post.productTags || post.product_tags || []);
}