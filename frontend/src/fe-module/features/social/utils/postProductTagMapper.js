export const TAGGABLE_PRODUCT_STATUSES = new Set(["ACTIVE", "PAUSED", "OUT_OF_STOCK", "DRAFT"]);

export function mapSellerListItemToPickerProduct(item) {
  if (!item?.productId) return null;

  const defaultPrice = item.effectivePrice ?? item.salePrice ?? item.price ?? 0;

  return {
    productId: item.productId,
    name: item.title || "Sản phẩm",
    category: item.categoryName || "",
    categoryId: item.categoryId || null,
    shopId: item.shopId || null,
    defaultPrice,
    imageUrl: item.thumbnailUrl || null,
    status: item.status,
  };
}

export function mapProductDetailToCatalogEntry(detail) {
  if (!detail?.productId) return null;

  const imageUrl = detail.media?.find((m) => m.mediaType === "IMAGE")?.mediaUrl
    || detail.media?.[0]?.mediaUrl
    || null;

  return {
    productId: detail.productId,
    name: detail.title || "Sản phẩm",
    category: detail.category?.name || "",
    categoryId: detail.category?.id || detail.categoryId || null,
    shopId: detail.shop?.id || detail.shopId || null,
    imageUrl,
    defaultPrice: detail.effectivePrice ?? detail.salePrice ?? detail.price ?? 0,
  };
}

export function mergeTagWithCatalog(tag, catalog) {
  if (!tag?.productId) return null;

  const price = Number(tag.price);
  const defaultPrice = catalog?.defaultPrice ?? 0;

  return {
    productId: tag.productId,
    name: tag.name || catalog?.name || "Sản phẩm",
    category: tag.category || catalog?.category || "",
    categoryId: tag.categoryId || catalog?.categoryId || null,
    shopId: tag.shopId || catalog?.shopId || null,
    imageUrl: tag.imageUrl || catalog?.imageUrl || null,
    defaultPrice,
    price: Number.isFinite(price) && price >= 0 ? price : defaultPrice,
    available: tag.available !== false,
  };
}