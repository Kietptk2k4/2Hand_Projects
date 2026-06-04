import { MOCK_PRODUCT_CATALOG } from "../constants/mockProductCatalog";

function findCatalogEntry(productId) {
  return MOCK_PRODUCT_CATALOG.find((item) => item.productId === productId);
}

/**
 * Enrich tag từ catalog MVP (Create/Edit + read post không gọi Commerce).
 */
export function enrichProductTag(tag) {
  if (!tag?.productId) return null;

  const catalog = findCatalogEntry(tag.productId);
  const price = Number(tag.price);
  const defaultPrice = catalog?.defaultPrice ?? 0;

  return {
    productId: tag.productId,
    price: Number.isFinite(price) && price >= 0 ? price : defaultPrice,
    name: catalog?.name || "Sản phẩm",
    category: catalog?.category || "",
    imageUrl: catalog?.imageUrl || null,
    defaultPrice,
  };
}

export function enrichProductTags(tags) {
  if (!Array.isArray(tags) || tags.length === 0) return [];
  return tags.map(enrichProductTag).filter(Boolean);
}
