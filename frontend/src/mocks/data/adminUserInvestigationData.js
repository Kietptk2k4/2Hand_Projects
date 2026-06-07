import { mockUsers } from "./authData";

const ENFORCEMENT_BY_USER_ID = {
  "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1": [
    {
      enforcement_id: "e1000000-0000-4000-8000-000000000001",
      user_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
      action_type: "RESTRICT",
      reason_code: "SPAM",
      description: "Spam comments on social feed",
      expires_at: "2026-12-31T23:59:59Z",
      enforced_by: "c0000000-0000-4000-8000-000000000099",
      status: "ACTIVE",
      created_at: "2026-05-20T10:00:00Z",
      updated_at: "2026-05-20T10:00:00Z",
      possibly_expired: false,
    },
  ],
};

const ENFORCEMENT_HISTORY_BY_USER_ID = {
  "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1": [
    {
      enforcement_id: "e1000000-0000-4000-8000-000000000001",
      user_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
      action_type: "RESTRICT",
      reason_code: "SPAM",
      description: "Spam comments on social feed",
      expires_at: "2026-12-31T23:59:59Z",
      enforced_by: "c0000000-0000-4000-8000-000000000099",
      status: "ACTIVE",
      created_at: "2026-05-20T10:00:00Z",
      updated_at: "2026-05-20T10:00:00Z",
      logs: [
        {
          log_id: "l1000000-0000-4000-8000-000000000001",
          old_status: null,
          new_status: "ACTIVE",
          admin_id: "c0000000-0000-4000-8000-000000000099",
          actor_type: "ADMIN",
          note: "Enforcement created",
          created_at: "2026-05-20T10:00:00Z",
        },
      ],
    },
    {
      enforcement_id: "e1000000-0000-4000-8000-000000000002",
      user_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
      action_type: "SUSPEND",
      reason_code: "POLICY_VIOLATION",
      description: "Temporary suspend for review",
      expires_at: null,
      enforced_by: "c0000000-0000-4000-8000-000000000099",
      status: "REVOKED",
      created_at: "2026-04-01T08:00:00Z",
      updated_at: "2026-04-15T12:00:00Z",
      logs: [
        {
          log_id: "l1000000-0000-4000-8000-000000000003",
          old_status: "ACTIVE",
          new_status: "REVOKED",
          admin_id: "c0000000-0000-4000-8000-000000000099",
          actor_type: "ADMIN",
          note: "False positive",
          created_at: "2026-04-15T12:00:00Z",
        },
        {
          log_id: "l1000000-0000-4000-8000-000000000002",
          old_status: null,
          new_status: "ACTIVE",
          admin_id: "c0000000-0000-4000-8000-000000000099",
          actor_type: "ADMIN",
          note: "Enforcement created",
          created_at: "2026-04-01T08:00:00Z",
        },
      ],
    },
  ],
};

export function getMockInvestigationProfile(userId) {
  const user = mockUsers.find((item) => item.id === userId);
  if (!user || user.status === "DELETED") return null;

  return {
    user_id: user.id,
    email: user.email,
    status: user.status,
    email_verified: Boolean(user.email_verified),
    phone_verified: false,
    last_login_at: user.last_login_at || null,
    created_at: "2026-01-01T00:00:00Z",
    display_name: user.display_name,
    avatar_url: user.avatar_url,
    bio: user.bio || "",
    website: user.website || "",
    is_private: Boolean(user.is_private),
    current_enforcements: (ENFORCEMENT_BY_USER_ID[userId] || []).map((item) => ({
      enforcement_id: item.enforcement_id,
      action_type: item.action_type,
      reason_code: item.reason_code,
      status: item.status,
      expires_at: item.expires_at,
      possibly_expired: item.possibly_expired,
    })),
  };
}

export function getMockCurrentEnforcements(userId) {
  return ENFORCEMENT_BY_USER_ID[userId] || [];
}

export function getMockEnforcementHistory(userId) {
  return ENFORCEMENT_HISTORY_BY_USER_ID[userId] || [];
}
const ACTION_TYPE_BY_ENDPOINT = {
  RESTRICT: "RESTRICT",
  SUSPEND: "SUSPEND",
  BAN: "BAN",
};

export function applyMockEnforcement(userId, actionType, body, enforcedBy) {
  const existing = ENFORCEMENT_BY_USER_ID[userId] || [];
  const duplicate = existing.find(
    (item) => item.status === "ACTIVE" && item.action_type === actionType,
  );
  if (duplicate) {
    return { conflict: true };
  }

  const enforcement = {
    enforcement_id: crypto.randomUUID(),
    user_id: userId,
    action_type: actionType,
    reason_code: body.reason_code,
    description: body.description,
    expires_at: body.expires_at || null,
    enforced_by: enforcedBy,
    status: "ACTIVE",
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
    possibly_expired: false,
    logs: [
      {
        log_id: crypto.randomUUID(),
        old_status: null,
        new_status: "ACTIVE",
        admin_id: enforcedBy,
        actor_type: "ADMIN",
        note: "Enforcement created",
        created_at: new Date().toISOString(),
      },
    ],
  };

  ENFORCEMENT_BY_USER_ID[userId] = [enforcement, ...existing];
  const history = ENFORCEMENT_HISTORY_BY_USER_ID[userId] || [];
  ENFORCEMENT_HISTORY_BY_USER_ID[userId] = [enforcement, ...history];
  return { enforcement };
}

export function revokeMockEnforcement(enforcementId, revokedBy, body = {}) {
  let revoked = null;
  Object.keys(ENFORCEMENT_BY_USER_ID).forEach((userId) => {
    ENFORCEMENT_BY_USER_ID[userId] = (ENFORCEMENT_BY_USER_ID[userId] || []).map((item) => {
      if (item.enforcement_id !== enforcementId) return item;
      revoked = {
        ...item,
        status: "REVOKED",
        updated_at: new Date().toISOString(),
        logs: [
          {
            log_id: crypto.randomUUID(),
            old_status: "ACTIVE",
            new_status: "REVOKED",
            admin_id: revokedBy,
            actor_type: "ADMIN",
            note: body.note || "Enforcement revoked",
            created_at: new Date().toISOString(),
          },
          ...(item.logs || []),
        ],
      };
      return revoked;
    }).filter((item) => item.status === "ACTIVE");
  });

  if (!revoked) return null;

  const userId = revoked.user_id;
  const history = ENFORCEMENT_HISTORY_BY_USER_ID[userId] || [];
  ENFORCEMENT_HISTORY_BY_USER_ID[userId] = history.map((item) =>
    item.enforcement_id === enforcementId ? revoked : item,
  );
  return revoked;
}