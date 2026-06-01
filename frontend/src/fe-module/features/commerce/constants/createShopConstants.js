export const SHOP_NAME_MAX = 255;

export const CREATE_SHOP_STEPS = [
  { id: 1, label: "Thương hiệu" },
  { id: 2, label: "Địa chỉ lấy hàng" },
];

export const EMPTY_PICKUP_FORM = {
  pickupName: "",
  phone: "",
  provinceCode: "",
  districtCode: "",
  wardCode: "",
  addressDetail: "",
};

export const EMPTY_CREATE_SHOP_FORM = {
  shopName: "",
  description: "",
  avatarUrl: "",
  coverUrl: "",
  includePickup: true,
  pickup: { ...EMPTY_PICKUP_FORM },
};

export const CREATE_SHOP_ERROR_MESSAGES = {
  "COMMERCE-400-VALIDATION": "Vui lòng kiểm tra lại thông tin shop.",
  "COMMERCE-400-MEDIA-URL": "URL ảnh không hợp lệ. (Upload MinIO — task sau)",
  "COMMERCE-409-SHOP-EXISTS": "Bạn đã có shop trên 2Hands. Mỗi tài khoản chỉ được tạo một shop.",
};

export function mapCreateShopApiError(error) {
  const code = String(error?.code ?? "");
  return CREATE_SHOP_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}

export function extractExistingShopIdFromError(error) {
  const fromData = error?.data?.shop_id ?? error?.data?.shopId;
  if (fromData) return fromData;

  const fromErrors = error?.errors?.find((item) => item.field === "shop_id");
  return fromErrors?.reason || null;
}
