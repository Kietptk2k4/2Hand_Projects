export function formatAccountDateTime(value) {
  if (!value) return null;
  try {
    return new Date(value).toLocaleString("vi-VN");
  } catch {
    return String(value);
  }
}