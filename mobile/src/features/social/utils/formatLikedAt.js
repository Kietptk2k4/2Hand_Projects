export function formatLikedAt(isoString) {
  if (!isoString) return "";
  try {
    return new Intl.DateTimeFormat("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date(isoString));
  } catch {
    return "";
  }
}
