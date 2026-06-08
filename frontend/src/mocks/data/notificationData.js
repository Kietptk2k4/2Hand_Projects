const DEFAULT_SETTINGS = [
  { eventType: "POST_LIKED", allowPush: true, allowEmail: false, allowInApp: true, explicitSetting: false },
  { eventType: "USER_FOLLOWED", allowPush: true, allowEmail: false, allowInApp: true, explicitSetting: false },
  { eventType: "ORDER_CREATED", allowPush: true, allowEmail: true, allowInApp: true, explicitSetting: false },
  { eventType: "PAYMENT_SUCCESS", allowPush: true, allowEmail: true, allowInApp: true, explicitSetting: false },
  { eventType: "SYSTEM_ANNOUNCEMENT_SENT", allowPush: true, allowEmail: false, allowInApp: true, explicitSetting: false },
];

function buildSeedNotifications(userId) {
  const now = Date.now();
  const suffix = userId.replace(/-/g, "").slice(-12);

  return [
    {
      id: `11111111-1111-4111-8111-${suffix}`,
      actorId: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
      type: "POST_LIKED",
      title: "Bai viet duoc thich",
      content: "Mot nguoi dung da thich bai viet cua ban.",
      referenceType: "POST",
      referenceId: "a1000000-0000-4000-8000-000000000001",
      metadata: JSON.stringify({ post_id: "a1000000-0000-4000-8000-000000000001" }),
      read: false,
      readAt: null,
      createdAt: new Date(now - 5 * 60_000).toISOString(),
      userId,
      isDeleted: false,
    },
    {
      id: `22222222-2222-4222-8222-${suffix}`,
      actorId: null,
      type: "ORDER_CREATED",
      title: "Don hang moi",
      content: "Don hang da duoc tao thanh cong.",
      referenceType: "ORDER",
      referenceId: "d1000000-0000-4000-8000-000000000010",
      metadata: JSON.stringify({ order_id: "d1000000-0000-4000-8000-000000000010" }),
      read: false,
      readAt: null,
      createdAt: new Date(now - 2 * 60 * 60_000).toISOString(),
      userId,
      isDeleted: false,
    },
    {
      id: `33333333-3333-4333-8333-${suffix}`,
      actorId: null,
      type: "SYSTEM_ANNOUNCEMENT_SENT",
      title: "Bao tri he thong",
      content: "2Hands se bao tri vao 22:00 hom nay.",
      referenceType: "SYSTEM_ANNOUNCEMENT",
      referenceId: "f1000000-0000-4000-8000-000000000099",
      metadata: JSON.stringify({ severity: "WARNING", is_pinned: true, dismissible: true }),
      read: true,
      readAt: new Date(now - 24 * 60 * 60_000).toISOString(),
      createdAt: new Date(now - 24 * 60 * 60_000).toISOString(),
      userId,
      isDeleted: false,
    },
  ];
}

const store = new Map();

function ensureUserState(userId) {
  if (!store.has(userId)) {
    store.set(userId, {
      notifications: buildSeedNotifications(userId),
      settings: DEFAULT_SETTINGS.map((item) => ({ ...item })),
      deviceTokens: [],
    });
  }
  return store.get(userId);
}

function visibleNotifications(userId) {
  return ensureUserState(userId).notifications.filter((item) => !item.isDeleted);
}

function toResponseItem(item) {
  return {
    id: item.id,
    actorId: item.actorId,
    type: item.type,
    title: item.title,
    content: item.content,
    referenceType: item.referenceType,
    referenceId: item.referenceId,
    metadata: item.metadata,
    read: item.read,
    readAt: item.readAt,
    createdAt: item.createdAt,
  };
}

function paginate(items, page, size) {
  const start = page * size;
  const slice = items.slice(start, start + size);
  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size) || 1);

  return {
    items: slice.map(toResponseItem),
    meta: {
      page,
      size,
      totalElements,
      totalPages,
      hasNext: start + size < totalElements,
    },
  };
}

export function getNotificationsForUser(userId, { page = 0, size = 20, unreadOnly = false } = {}) {
  let items = visibleNotifications(userId).sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  if (unreadOnly) {
    items = items.filter((item) => !item.read);
  }

  return paginate(items, page, size);
}

export function getUnreadCountForUser(userId) {
  return visibleNotifications(userId).filter((item) => !item.read).length;
}

export function markNotificationReadForUser(userId, notificationId) {
  const state = ensureUserState(userId);
  const item = state.notifications.find((entry) => entry.id === notificationId && entry.userId === userId);

  if (!item || item.isDeleted) {
    return { error: "NOTIFICATION-404", status: 404 };
  }

  if (!item.read) {
    item.read = true;
    item.readAt = new Date().toISOString();
  }

  return {
    data: {
      notificationId: item.id,
      read: true,
      readAt: item.readAt,
      alreadyRead: false,
    },
  };
}

export function markAllNotificationsReadForUser(userId) {
  const state = ensureUserState(userId);
  let updatedCount = 0;

  state.notifications.forEach((item) => {
    if (!item.isDeleted && !item.read) {
      item.read = true;
      item.readAt = new Date().toISOString();
      updatedCount += 1;
    }
  });

  return { data: { updatedCount } };
}

export function deleteNotificationForUser(userId, notificationId) {
  const state = ensureUserState(userId);
  const item = state.notifications.find((entry) => entry.id === notificationId && entry.userId === userId);

  if (!item) {
    return { error: "NOTIFICATION-404", status: 404 };
  }

  const alreadyDeleted = item.isDeleted;
  item.isDeleted = true;

  return {
    data: {
      notificationId: item.id,
      deleted: true,
      alreadyDeleted,
    },
  };
}

export function dismissAnnouncementForUser(userId, notificationId) {
  const state = ensureUserState(userId);
  const item = state.notifications.find((entry) => entry.id === notificationId && entry.userId === userId);

  if (!item || item.isDeleted) {
    return { error: "NOTIFICATION-404", status: 404 };
  }

  if (item.referenceType !== "SYSTEM_ANNOUNCEMENT") {
    return { error: "NOTIFICATION-400", status: 400 };
  }

  let metadata = {};
  try {
    metadata = JSON.parse(item.metadata || "{}");
  } catch {
    metadata = {};
  }

  if (metadata.dismissible !== true) {
    return { error: "NOTIFICATION-409", status: 409 };
  }

  const alreadyDismissed = item.isDeleted;
  item.isDeleted = true;

  return {
    data: {
      notificationId: item.id,
      dismissed: true,
      alreadyDismissed,
    },
  };
}

export function getNotificationSettingsForUser(userId) {
  const state = ensureUserState(userId);
  return { settings: state.settings.map((item) => ({ ...item })) };
}

export function updateNotificationSettingForUser(userId, eventType, body) {
  const state = ensureUserState(userId);
  const normalized = String(eventType || "").trim().toUpperCase();
  const existing = state.settings.find((item) => item.eventType === normalized);
  const next = {
    eventType: normalized,
    allowPush: Boolean(body?.allowPush),
    allowEmail: Boolean(body?.allowEmail),
    allowInApp: Boolean(body?.allowInApp),
    explicitSetting: true,
  };

  if (existing) {
    Object.assign(existing, next);
  } else {
    state.settings.push(next);
  }

  return { data: next };
}

export function getDeviceTokensForUser(userId) {
  const state = ensureUserState(userId);

  return {
    items: state.deviceTokens.map((item) => ({
      id: item.id,
      deviceType: item.deviceType,
      maskedDeviceToken: item.maskedDeviceToken,
      active: item.active,
      lastUsedAt: item.lastUsedAt,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    })),
  };
}

export function registerDeviceTokenForUser(userId, body) {
  const state = ensureUserState(userId);
  const deviceToken = String(body?.deviceToken || "").trim();
  const deviceType = body?.deviceType || "WEB";

  if (!deviceToken) {
    return { error: "NOTIFICATION-400", status: 400 };
  }

  const existing = state.deviceTokens.find((item) => item.rawToken === deviceToken);
  if (existing) {
    existing.active = true;
    existing.updatedAt = new Date().toISOString();
    existing.lastUsedAt = new Date().toISOString();

    return {
      data: {
        id: existing.id,
        deviceType: existing.deviceType,
        active: true,
        createdAt: existing.createdAt,
        updatedAt: existing.updatedAt,
        lastUsedAt: existing.lastUsedAt,
        alreadyRegistered: true,
      },
    };
  }

  const createdAt = new Date().toISOString();
  const item = {
    id: crypto.randomUUID(),
    deviceType,
    rawToken: deviceToken,
    maskedDeviceToken: `****${deviceToken.slice(-4)}`,
    active: true,
    createdAt,
    updatedAt: createdAt,
    lastUsedAt: createdAt,
  };

  state.deviceTokens.push(item);

  return {
    data: {
      id: item.id,
      deviceType: item.deviceType,
      active: true,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
      lastUsedAt: item.lastUsedAt,
      alreadyRegistered: false,
    },
  };
}

export function revokeDeviceTokenForUser(userId, deviceToken) {
  const state = ensureUserState(userId);
  const item = state.deviceTokens.find((entry) => entry.rawToken === deviceToken);

  if (!item) {
    return { error: "NOTIFICATION-404", status: 404 };
  }

  const alreadyRevoked = !item.active;
  item.active = false;
  item.updatedAt = new Date().toISOString();

  return {
    data: {
      id: item.id,
      active: false,
      alreadyRevoked,
    },
  };
}
