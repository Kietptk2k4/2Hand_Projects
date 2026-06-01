export const MOCK_SHOP_BUILDTECH_ID = "s1000000-0000-4000-8000-000000000001";
export const MOCK_SHOP_AQUAFLOW_ID = "s1000000-0000-4000-8000-000000000006";
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
    shop_id: MOCK_SHOP_BUILDTECH_ID,
    shop_name: "BuildTech Supplies",
    description:
      "Chuyên cung cấp dụng cụ điện và thiết bị thi công chất lượng cao cho thợ và nhà thầu trên 2Hands.",
    avatar_url: "https://picsum.photos/seed/shop-buildtech-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-buildtech-cover/1200/400",
    rating_avg: 4.8,
    rating_count: 128,
    shop_vacation: false,
    vacation_message: null,
    status: "ACTIVE",
  },
  {
    shop_id: MOCK_SHOP_AQUAFLOW_ID,
    shop_name: "AquaFlow Plumbing",
    description:
      "Giải pháp ống nước và phụ kiện hệ thống cấp thoát nước cho dự án dân dụng và công nghiệp.",
    avatar_url: "https://picsum.photos/seed/shop-aquaflow-avatar/200/200",
    cover_url: "https://picsum.photos/seed/shop-aquaflow-cover/1200/400",
    rating_avg: 4.5,
    rating_count: 42,
    shop_vacation: true,
    vacation_message:
      "Shop tạm thời không nhận đơn hàng mới. Bạn vẫn có thể xem sản phẩm; đơn sẽ được xử lý sau khi shop mở lại.",
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
