const FALLBACK_BUYER_NAME = "Người mua";

function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapReviewBuyerFields(review) {
  if (!review) {
    return {
      buyerId: null,
      buyerDisplayName: FALLBACK_BUYER_NAME,
      buyerAvatarUrl: null,
    };
  }

  return {
    buyerId: pick(review, "buyerId", "buyer_id") ?? null,
    buyerDisplayName:
      pick(review, "buyerDisplayName", "buyer_display_name") || FALLBACK_BUYER_NAME,
    buyerAvatarUrl: pick(review, "buyerAvatarUrl", "buyer_avatar_url") ?? null,
  };
}

export function mapReviewShopFields(shop) {
  if (!shop) return null;

  return {
    shopId: pick(shop, "shopId", "shop_id") ?? null,
    shopName: pick(shop, "shopName", "shop_name") ?? "",
    avatarUrl: pick(shop, "avatarUrl", "avatar_url") ?? null,
    sellerId: pick(shop, "sellerId", "seller_id") ?? null,
  };
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
    shopId: pick(data, "shopId", "shop_id") ?? null,
    shopName: pick(data, "shopName", "shop_name") ?? "",
    avatarUrl: pick(data, "avatarUrl", "avatar_url") ?? null,
    sellerId: pick(data, "sellerId", "seller_id") ?? null,
  };
}