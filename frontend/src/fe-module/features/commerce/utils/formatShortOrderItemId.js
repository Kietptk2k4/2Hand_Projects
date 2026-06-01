export function formatShortOrderItemId(orderItemId) {
  if (!orderItemId) return "";
  const suffix = orderItemId.replace(/-/g, "").slice(-6);
  return `…${suffix}`;
}
