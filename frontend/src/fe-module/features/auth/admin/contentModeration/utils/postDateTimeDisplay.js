export function formatPostListDateTime(value) {
  if (!value) {
    return { time: "—", date: "—" };
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return { time: "—", date: "—" };
  }

  return {
    time: date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    }),
    date: date.toLocaleDateString("vi-VN"),
  };
}
