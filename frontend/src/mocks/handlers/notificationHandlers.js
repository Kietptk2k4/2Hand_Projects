import { delay, http, HttpResponse } from "msw";
import {
  deleteNotificationForUser,
  dismissAnnouncementForUser,
  getDeviceTokensForUser,
  getNotificationSettingsForUser,
  getNotificationsForUser,
  getUnreadCountForUser,
  markAllNotificationsReadForUser,
  markNotificationReadForUser,
  registerDeviceTokenForUser,
  revokeDeviceTokenForUser,
  updateNotificationSettingForUser,
} from "../data/notificationData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("NOTIFICATION-401", "Authentication required."), {
        status: 401,
      }),
    };
  }
  return { user };
}

function notificationError(result) {
  return HttpResponse.json(apiError(result.error, "Notification request failed."), {
    status: result.status,
  });
}

export const notificationHandlers = [
  http.get("*/api/v1/notification/notifications/unread-count", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    return HttpResponse.json(
      apiSuccess(200, "Unread notification count retrieved successfully", {
        count: getUnreadCountForUser(auth.user.id),
      })
    );
  }),
  http.get("*/api/v1/notification/notifications/unread", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") || 0);
    const size = Number(url.searchParams.get("size") || 20);
    return HttpResponse.json(
      apiSuccess(200, "Unread notifications retrieved successfully", {
        ...getNotificationsForUser(auth.user.id, { page, size, unreadOnly: true }),
      })
    );
  }),
  http.get("*/api/v1/notification/notifications", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") || 0);
    const size = Number(url.searchParams.get("size") || 20);
    return HttpResponse.json(
      apiSuccess(200, "Notifications retrieved successfully", {
        ...getNotificationsForUser(auth.user.id, { page, size }),
      })
    );
  }),
  http.patch("*/api/v1/notification/notifications/read-all", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    return HttpResponse.json(
      apiSuccess(200, "All notifications marked as read", markAllNotificationsReadForUser(auth.user.id).data)
    );
  }),
  http.patch("*/api/v1/notification/notifications/:notificationId/read", async ({ request, params }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = markNotificationReadForUser(auth.user.id, params.notificationId);
    if (result.error) return notificationError(result);
    return HttpResponse.json(apiSuccess(200, "Notification marked as read", result.data));
  }),
  http.post("*/api/v1/notification/notifications/:notificationId/dismiss", async ({ request, params }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = dismissAnnouncementForUser(auth.user.id, params.notificationId);
    if (result.error) return notificationError(result);
    return HttpResponse.json(
      apiSuccess(200, "Announcement notification dismissed successfully", result.data)
    );
  }),
  http.delete("*/api/v1/notification/notifications/:notificationId", async ({ request, params }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = deleteNotificationForUser(auth.user.id, params.notificationId);
    if (result.error) return notificationError(result);
    return HttpResponse.json(apiSuccess(200, "Notification deleted successfully", result.data));
  }),
  http.get("*/api/v1/notification/notification-settings", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    return HttpResponse.json(
      apiSuccess(200, "Notification settings retrieved successfully", getNotificationSettingsForUser(auth.user.id))
    );
  }),
  http.put("*/api/v1/notification/notification-settings/:eventType", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = updateNotificationSettingForUser(auth.user.id, params.eventType, body);
    return HttpResponse.json(apiSuccess(200, "Notification setting updated successfully", result.data));
  }),
  http.get("*/api/v1/notification/device-tokens", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    return HttpResponse.json(
      apiSuccess(200, "Device tokens retrieved successfully", getDeviceTokensForUser(auth.user.id))
    );
  }),
  http.post("*/api/v1/notification/device-tokens", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = registerDeviceTokenForUser(auth.user.id, body);
    if (result.error) return notificationError(result);
    return HttpResponse.json(apiSuccess(200, "Device token registered successfully", result.data));
  }),
  http.delete("*/api/v1/notification/device-tokens/:deviceToken", async ({ request, params }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = revokeDeviceTokenForUser(auth.user.id, decodeURIComponent(params.deviceToken));
    if (result.error) return notificationError(result);
    return HttpResponse.json(apiSuccess(200, "Device token revoked successfully", result.data));
  }),
];