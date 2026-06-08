import { notificationApiClient } from "../../../services/http/notificationApiClient";
import { mapAxiosError, unwrapResponse } from "./notificationApiResponse";

const NOTIFICATION_PREFIX = "/api/v1/notification";

export async function fetchNotifications({ page = 0, size = 20 } = {}) {
  try {
    const response = await notificationApiClient.get(`${NOTIFICATION_PREFIX}/notifications`, {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchUnreadNotifications({ page = 0, size = 20 } = {}) {
  try {
    const response = await notificationApiClient.get(`${NOTIFICATION_PREFIX}/notifications/unread`, {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchUnreadNotificationCount() {
  try {
    const response = await notificationApiClient.get(`${NOTIFICATION_PREFIX}/notifications/unread-count`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function markNotificationAsRead(notificationId) {
  try {
    const response = await notificationApiClient.patch(
      `${NOTIFICATION_PREFIX}/notifications/${notificationId}/read`
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function markAllNotificationsAsRead() {
  try {
    const response = await notificationApiClient.patch(`${NOTIFICATION_PREFIX}/notifications/read-all`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deleteNotification(notificationId) {
  try {
    const response = await notificationApiClient.delete(`${NOTIFICATION_PREFIX}/notifications/${notificationId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function dismissAnnouncementNotification(notificationId) {
  try {
    const response = await notificationApiClient.post(
      `${NOTIFICATION_PREFIX}/notifications/${notificationId}/dismiss`
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchNotificationSettings() {
  try {
    const response = await notificationApiClient.get(`${NOTIFICATION_PREFIX}/notification-settings`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateNotificationSetting(eventType, { allowPush, allowEmail, allowInApp }) {
  try {
    const response = await notificationApiClient.put(
      `${NOTIFICATION_PREFIX}/notification-settings/${encodeURIComponent(eventType)}`,
      { allowPush, allowEmail, allowInApp }
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchUserDeviceTokens() {
  try {
    const response = await notificationApiClient.get(`${NOTIFICATION_PREFIX}/device-tokens`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function registerDeviceToken({ deviceType, deviceToken }) {
  try {
    const response = await notificationApiClient.post(`${NOTIFICATION_PREFIX}/device-tokens`, {
      deviceType,
      deviceToken,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function revokeDeviceToken(deviceToken) {
  try {
    const response = await notificationApiClient.delete(
      `${NOTIFICATION_PREFIX}/device-tokens/${encodeURIComponent(deviceToken)}`
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
