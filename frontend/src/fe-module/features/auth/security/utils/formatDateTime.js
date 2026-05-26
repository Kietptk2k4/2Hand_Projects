export function formatDateTime(value) {
  if (!value) return "Chua cap nhat";
  try {
    return new Date(value).toLocaleString("vi-VN");
  } catch {
    return value;
  }
}
