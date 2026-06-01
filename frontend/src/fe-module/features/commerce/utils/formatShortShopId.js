export function formatShortShopId(shopId) {
  if (!shopId) return "";
  const suffix = shopId.replace(/-/g, "").slice(-6);
  return `SHP-${suffix}`;
}

export function formatShortSellerId(sellerId) {
  if (!sellerId) return "";
  const suffix = sellerId.replace(/-/g, "").slice(-6);
  return `USR-${suffix}`;
}
