export const MOCK_SHOP_LAN_CLOSET_ID = "s1000000-0000-4000-8000-000000000001";
export const MOCK_SHOP_VACATION_ID = "s1000000-0000-4000-8000-000000000006";

/** @deprecated use MOCK_SHOP_LAN_CLOSET_ID */
export const MOCK_SHOP_BUILDTECH_ID = MOCK_SHOP_LAN_CLOSET_ID;
/** @deprecated use MOCK_SHOP_VACATION_ID */
export const MOCK_SHOP_AQUAFLOW_ID = MOCK_SHOP_VACATION_ID;
export const MOCK_SHOP_SUSPENDED_ID = "s1000000-0000-4000-8000-000000000099";
export const MOCK_SHOP_EMPTY_ID = "s1000000-0000-4000-8000-000000000007";

export const MOCK_SHOP_IDS = {
  BUILDTECH: MOCK_SHOP_BUILDTECH_ID,
  AQUAFLOW: MOCK_SHOP_AQUAFLOW_ID,
  SUSPENDED: MOCK_SHOP_SUSPENDED_ID,
  EMPTY: MOCK_SHOP_EMPTY_ID,
};

const shopRecords = [
  {
    shop_id: MOCK_SHOP_LAN_CLOSET_ID,
    shop_name: "Tu do cua Lan",
    description:
      "Tu do thoi trang nu second-hand — ao, vay, giay va tui xach tu Lan.",
    avatar_url: "https://picsum.photos/seed/shop-lan-closet-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-lan-closet-cover/1200/400",
    rating_avg: 4.8,
    rating_count: 128,
    shop_vacation: false,
    vacation_message: null,
    status: "ACTIVE",
  },
  {
    shop_id: MOCK_SHOP_VACATION_ID,
    shop_name: "Closet Nghi Phep",
    description:
      "Tu do thoi trang tam nghi — ban van co the xem san pham, don se xu ly sau.",
    avatar_url: "https://picsum.photos/seed/shop-vacation-closet-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-vacation-closet-cover/1200/400",
    rating_avg: 4.5,
    rating_count: 42,
    shop_vacation: true,
    vacation_message:
      "Shop tạm thời không nhận đơn hàng mới. Bạn vẫn có thể xem sản phẩm; đơn sẽ được xử lý sau khi shop mở lại.",
    status: "ACTIVE",
  },
  {
    shop_id: "s1000000-0000-4000-8000-000000000002",
    shop_name: "Vintage Closet",
    description: "Do vintage va second-hand doc dao tu nguon uy tin.",
    avatar_url: "https://picsum.photos/seed/shop-vintage-closet-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-vintage-closet-cover/1200/400",
    rating_avg: 4.7,
    rating_count: 64,
    shop_vacation: false,
    vacation_message: null,
    status: "ACTIVE",
  },
  {
    shop_id: "s1000000-0000-4000-8000-000000000003",
    shop_name: "Streetwear 2Hand",
    description: "Streetwear, hoodie va sneaker second-hand.",
    avatar_url: "https://picsum.photos/seed/shop-streetwear-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-streetwear-cover/1200/400",
    rating_avg: 4.6,
    rating_count: 38,
    shop_vacation: false,
    vacation_message: null,
    status: "ACTIVE",
  },
  {
    shop_id: MOCK_SHOP_EMPTY_ID,
    shop_name: "ProStock Empty Demo",
    description: "Shop demo không có sản phẩm — dùng để kiểm tra empty state trên storefront.",
    avatar_url: "https://picsum.photos/seed/shop-empty-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-empty-cover/1200/400",
    rating_avg: 4.0,
    rating_count: 5,
    shop_vacation: false,
    vacation_message: null,
    status: "ACTIVE",
  },
  {
    shop_id: MOCK_SHOP_SUSPENDED_ID,
    shop_name: "Suspended Shop",
    description: "",
    avatar_url: "",
    cover_url: "",
    rating_avg: 0,
    rating_count: 0,
    shop_vacation: false,
    vacation_message: null,
    status: "SUSPENDED",
  },
];

const shopById = new Map(shopRecords.map((shop) => [shop.shop_id, shop]));

export function getShopById(shopId) {
  return shopById.get(shopId) || null;
}

export function getPublicShopSummary(shopId) {
  const shop = getShopById(shopId);
  if (!shop || shop.status !== "ACTIVE") return null;
  const {
    shop_id,
    shop_name,
    description,
    avatar_url,
    cover_url,
    rating_avg,
    rating_count,
    shop_vacation,
    vacation_message,
  } = shop;
  return {
    shop_id,
    shop_name,
    description,
    avatar_url,
    cover_url,
    rating_avg,
    rating_count,
    shop_vacation,
    vacation_message,
  };
}

/** Đăng ký shop mới từ CreateShop — storefront GET /shops/{id}/products */
export function registerShopFromCreate(data) {
  const shop = {
    shop_id: data.shop_id,
    shop_name: data.shop_name,
    description: data.description || "",
    avatar_url: data.avatar_url || "",
    cover_url: data.cover_url || "",
    rating_avg: 0,
    rating_count: 0,
    shop_vacation: Boolean(data.is_vacation),
    vacation_message: data.vacation_message ?? null,
    status: data.status || "ACTIVE",
  };
  shopById.set(shop.shop_id, shop);
  return shop;
}

/** Sync public storefront after seller updates profile or vacation (rating/status unchanged). */
export function updatePublicShopFromSellerRecord(record) {
  const shop = shopById.get(record.shop_id);
  if (!shop) return null;

  const updated = {
    ...shop,
    shop_name: record.shop_name,
    description: record.description ?? "",
    avatar_url: record.avatar_url || "",
    cover_url: record.cover_url || "",
    shop_vacation: Boolean(record.is_vacation),
    vacation_message: record.is_vacation ? record.vacation_message ?? null : null,
  };

  shopById.set(record.shop_id, updated);
  return updated;
}
