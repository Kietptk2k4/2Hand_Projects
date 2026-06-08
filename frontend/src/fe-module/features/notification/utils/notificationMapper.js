import { NOTIFICATION_EVENT_LABELS } from "../constants/notificationConstants";

export function parseNotificationMetadata(rawMetadata) {
  if (!rawMetadata) return {};
  if (typeof rawMetadata === "object") return rawMetadata;
  try {
    const parsed = JSON.parse(rawMetadata);
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

export function mapNotificationItem(item) {
  const metadata = parseNotificationMetadata(item?.metadata);

  return {
    id: item?.id,
    actorId: item?.actorId,
    type: item?.type,
    typeLabel: NOTIFICATION_EVENT_LABELS[item?.type] || item?.type || "Thông báo",
    title: item?.title || "",
    content: item?.content || "",
    referenceType: item?.referenceType,
    referenceId: item?.referenceId,
    metadata,
    read: Boolean(item?.read),
    readAt: item?.readAt,
    createdAt: item?.createdAt,
    isPinnedAnnouncement:
      item?.referenceType === "SYSTEM_ANNOUNCEMENT" && metadata?.is_pinned === true,
    isDismissibleAnnouncement:
      item?.referenceType === "SYSTEM_ANNOUNCEMENT" && metadata?.dismissible === true,
    severity: metadata?.severity || "INFO",
  };
}

export function mapNotificationListResponse(data) {
  const items = Array.isArray(data?.items) ? data.items.map(mapNotificationItem) : [];
  const meta = data?.meta || {};

  return {
    items,
    meta: {
      page: Number(meta.page ?? 0),
      size: Number(meta.size ?? items.length),
      totalElements: Number(meta.totalElements ?? items.length),
      totalPages: Number(meta.totalPages ?? 1),
      hasNext: Boolean(meta.hasNext),
    },
  };
}

export function mapNotificationSettingItem(item) {
  return {
    eventType: item?.eventType,
    label: NOTIFICATION_EVENT_LABELS[item?.eventType] || item?.eventType,
    allowPush: Boolean(item?.allowPush),
    allowEmail: Boolean(item?.allowEmail),
    allowInApp: Boolean(item?.allowInApp),
    explicitSetting: Boolean(item?.explicitSetting),
  };
}

export function mapDeviceTokenItem(item) {
  return {
    id: item?.id,
    deviceType: item?.deviceType,
    maskedDeviceToken: item?.maskedDeviceToken,
    active: Boolean(item?.active),
    lastUsedAt: item?.lastUsedAt,
    createdAt: item?.createdAt,
    updatedAt: item?.updatedAt,
  };
}
