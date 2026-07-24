/**
 * Builds payload for the in-app Instagram-style notification toast.
 */
export function buildNewNotificationToastPayload(delta, notification) {
  const safeDelta = Math.max(1, Number(delta) || 1);
  const titleFromNotification = notification?.title?.trim() || "";
  const contentFromNotification = notification?.content?.trim() || "";

  if (safeDelta > 1) {
    return {
      delta: safeDelta,
      notification: notification ?? null,
      title: `Bạn có ${safeDelta} thông báo mới`,
      content: titleFromNotification || "Nhấn để xem chi tiết trong chuông thông báo",
    };
  }

  return {
    delta: 1,
    notification: notification ?? null,
    title: titleFromNotification || "Bạn có thông báo mới",
    content: contentFromNotification,
  };
}

/** @deprecated Prefer buildNewNotificationToastPayload for rich toast UI. */
export function buildNewNotificationToastMessage(delta, notification) {
  return buildNewNotificationToastPayload(delta, notification).title;
}
