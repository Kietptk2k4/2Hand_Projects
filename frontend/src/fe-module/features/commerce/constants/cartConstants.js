export const UNAVAILABLE_REASON_LABELS = {
  OUT_OF_STOCK: "Hết hàng",
  SHOP_ON_VACATION: "Shop đang nghỉ",
};

export function getUnavailableReasonLabel(reason) {
  if (!reason) return "";
  return UNAVAILABLE_REASON_LABELS[reason] || "Sản phẩm không khả dụng";
}
