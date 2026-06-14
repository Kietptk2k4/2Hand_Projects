/**
 * Builds the toast message shown when polling detects new unread notifications.
 */
export function buildNewNotificationToastMessage(delta, notification) {
  const safeDelta = Math.max(1, Number(delta) || 1);

  if (safeDelta > 1) {
    return `Bạn có ${safeDelta} thông báo mới`;
  }

  const title = notification?.title?.trim();
  return title || "Bạn có thông báo mới";
}
