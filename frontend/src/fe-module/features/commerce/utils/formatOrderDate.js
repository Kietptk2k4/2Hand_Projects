export function formatOrderDate(iso) {
  if (!iso) return "";
  try {
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) return iso;
    return date.toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

export function formatShortOrderId(orderId) {
  if (!orderId) return "";
  const suffix = orderId.replace(/-/g, "").slice(-6);
  return `#${suffix}`;
}
