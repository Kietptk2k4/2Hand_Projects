export const DEFAULT_SHIPMENT_TYPE = "STANDARD";

export const DEFAULT_PAYMENT_METHOD = "PAYOS";

export const SHIPMENT_TYPES = [
  { value: "STANDARD", label: "Giao hàng tiêu chuẩn" },
  { value: "EXPRESS", label: "Giao hàng nhanh" },
  { value: "SAME_DAY", label: "Giao trong ngày" },
];

export const PAYMENT_METHODS = [
  { value: "PAYOS", label: "Thanh toán PayOS" },
  { value: "COD", label: "Thanh toán khi nhận hàng (COD)" },
];

export const QUOTE_DISCLAIMER =
  "Giá và phí vận chuyển mang tính tạm tính; có thể thay đổi khi đặt hàng.";

export const CHECKOUT_IDEMPOTENCY_STORAGE_KEY = "commerce:checkout-idempotency-key";

/** Mock hint per seller — khớp commerceCheckoutData.js */
export const SHIPPING_FEE_HINT_PER_SELLER = {
  STANDARD: 30000,
  EXPRESS: 52500,
  SAME_DAY: 80000,
};
