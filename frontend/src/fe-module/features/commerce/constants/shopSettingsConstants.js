export const SHOP_NAME_MAX = 255;
export const VACATION_MESSAGE_MAX = 500;

export const EMPTY_SHOP_SETTINGS_FORM = {
  shopName: "",
  description: "",
  avatarUrl: "",
  coverUrl: "",
  isVacation: false,
  vacationMessage: "",
};

export const SHOP_SETTINGS_ERROR_MESSAGES = {
  "COMMERCE-400-VALIDATION": "Vui lòng kiểm tra lại thông tin cửa hàng.",
  "COMMERCE-400-MEDIA-URL": "URL ảnh không hợp lệ. (Upload MinIO — task sau)",
  "COMMERCE-404-SHOP": "Bạn chưa có cửa hàng trên 2Hands.",
};

export function mapShopSettingsApiError(error) {
  const code = String(error?.code ?? "");
  return SHOP_SETTINGS_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}
