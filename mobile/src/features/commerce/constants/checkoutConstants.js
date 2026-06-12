export const DEFAULT_SHIPMENT_TYPE = "STANDARD";
export const DEFAULT_SHIPMENT_LABEL = "Giao hàng tiêu chuẩn";

function parseEnvBoolean(value, defaultValue) {
  if (value === undefined || value === "") return defaultValue;
  return value === "true" || value === "1";
}

export const CHECKOUT_COD_ONLY_ENABLED = parseEnvBoolean(
  process.env.EXPO_PUBLIC_COMMERCE_CHECKOUT_COD_ONLY_ENABLED,
  false
);

export const DEFAULT_PAYMENT_METHOD = CHECKOUT_COD_ONLY_ENABLED ? "COD" : "PAYOS";

export const PAYMENT_METHODS = CHECKOUT_COD_ONLY_ENABLED
  ? [{ value: "COD", label: "Thanh toán khi nhận hàng (COD)" }]
  : [
      { value: "PAYOS", label: "Thanh toán PayOS" },
      { value: "COD", label: "Thanh toán khi nhận hàng (COD)" },
    ];

export const QUOTE_DISCLAIMER =
  "Giá và phí vận chuyển mang tính tạm tính; có thể thay đổi khi đặt hàng.";

export const CHECKOUT_IDEMPOTENCY_STORAGE_KEY = "commerce:checkout-idempotency-key";