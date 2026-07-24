export function isProductOnSale(product) {
  return (
    product?.salePrice != null &&
    product?.price != null &&
    Number(product.salePrice) < Number(product.price)
  );
}

export function isProductOutOfStock(product) {
  return (
    !product?.inventorySummary?.inStock || product?.status === "OUT_OF_STOCK"
  );
}

export function isOwnListing(product, currentUserId) {
  if (!product?.sellerId || !currentUserId) return false;
  return String(product.sellerId) === String(currentUserId);
}

export function isAddToCartDisabled(product, currentUserId) {
  return (
    isProductOutOfStock(product) ||
    Boolean(product?.shopVacation) ||
    isOwnListing(product, currentUserId)
  );
}

export function getStockLabel(product) {
  const inv = product?.inventorySummary;
  if (!inv) return "";

  if (!inv.inStock || product?.status === "OUT_OF_STOCK") {
    return "Hết hàng";
  }
  if (inv.lowStock) {
    return `Sắp hết hàng · còn ${inv.stockQuantity ?? 0}`;
  }
  return "Còn hàng";
}
