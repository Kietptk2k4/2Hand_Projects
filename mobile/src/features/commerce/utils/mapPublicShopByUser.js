function pick(data, ...keys) {
  if (!data) return null;
  for (const key of keys) {
    const value = data[key];
    if (value !== undefined && value !== null && value !== "") {
      return value;
    }
  }
  return null;
}

export function mapPublicShopByUserResponse(data) {
  if (!data) {
    return {
      hasShop: false,
      shopId: null,
      shopName: "",
      avatarUrl: null,
      sellerId: null,
    };
  }

  return {
    hasShop: Boolean(data.has_shop ?? data.hasShop),
    shopId: pick(data, "shopId", "shop_id"),
    shopName: pick(data, "shopName", "shop_name") ?? "",
    avatarUrl: pick(data, "avatarUrl", "avatar_url"),
    sellerId: pick(data, "sellerId", "seller_id"),
  };
}
