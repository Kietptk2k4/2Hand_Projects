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

export function isAddToCartDisabled(product) {
  return isProductOutOfStock(product) || Boolean(product?.shopVacation);
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
