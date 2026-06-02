export const UNAVAILABLE_REASON_LABELS = {
  CART_ITEM_REMOVED: "Đã xóa khỏi giỏ",
  INVALID_PRODUCT: "Sản phẩm không còn bán",
  OUT_OF_STOCK: "Hết hàng",
  PRODUCT_NOT_ACTIVE: "Sản phẩm không hoạt động",
  SHOP_NOT_ACTIVE: "Shop không hoạt động",
  CATEGORY_INACTIVE: "Danh mục không khả dụng",
  ACTIVE_PRICE_MISSING: "Chưa có giá bán",
  SHOP_ON_VACATION: "Shop đang nghỉ",
  PRODUCT_NOT_FOUND: "Không tìm thấy sản phẩm",
};

export const CART_VALIDATE_REASON_LABELS = {
  ...UNAVAILABLE_REASON_LABELS,
};

export function getUnavailableReasonLabel(reason) {
  if (!reason) return "";
  return UNAVAILABLE_REASON_LABELS[reason] || "Sản phẩm không khả dụng";
}

export function getCartValidateReasonLabel(reason) {
  if (!reason) return "Sản phẩm không khả dụng";
  return CART_VALIDATE_REASON_LABELS[reason] || getUnavailableReasonLabel(reason);
}
