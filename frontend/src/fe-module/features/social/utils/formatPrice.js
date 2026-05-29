export function formatVndPrice(value) {
  const amount = Number(value);
  if (Number.isNaN(amount)) return "";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
}
