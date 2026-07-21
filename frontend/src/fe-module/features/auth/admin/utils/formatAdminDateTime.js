export function formatAdminDateTime(value) {
  if (!value) {
    return { time: "—", date: "Chưa cập nhật" };
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return { time: "—", date: String(value) };
  }

  const time = date.toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
  const dateLabel = date.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });

  return { time, date: dateLabel };
}
