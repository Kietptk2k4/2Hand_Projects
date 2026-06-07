import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import {
  applyMockEnforcement,
  getMockCurrentEnforcements,
  getMockEnforcementHistory,
  getMockInvestigationProfile,
  revokeMockEnforcement,
} from "../data/adminUserInvestigationData";
import { apiError, apiSuccess } from "../utils/response";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function getActorFromRequest(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  if (token.includes("expired-access")) return null;
  return mockUsers.find((item) => token.includes(item.id)) || null;
}

function isAdminActor(actor) {
  return Boolean(actor?.is_admin);
}

function isValidUuid(value) {
  return UUID_RE.test(value || "");
}

function getTargetUser(userId) {
  const user = mockUsers.find((u) => u.id === userId);
  if (!user || user.status === "DELETED") return null;
  return user;
}

function paginateEnforcementHistory(items, page, size) {
  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * size;
  return {
    enforcements: items.slice(start, start + size),
    page: safePage,
    size,
    total_elements: totalElements,
    total_pages: totalPages,
  };
}

export const adminUserInvestigationHandlers = [
  http.get("*/admin/api/v1/users/:userId/profile", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    const profile = getMockInvestigationProfile(userId);
    if (!profile) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "User investigation profile retrieved successfully", profile),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/users/:userId/enforcements/current", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Current user enforcements retrieved successfully", {
        user_id: userId,
        enforcements: getMockCurrentEnforcements(userId),
      }),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/users/:userId/enforcements/history", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") || "1");
    const size = Number(url.searchParams.get("size") || "20");
    const history = getMockEnforcementHistory(userId);
    const paged = paginateEnforcementHistory(history, page, size);

    return HttpResponse.json(
      apiSuccess(200, "User enforcement history retrieved successfully", {
        user_id: userId,
        ...paged,
      }),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/users/:userId/suspend", async ({ request, params }) => {
    await delay(400);
    const actor = getActorFromRequest(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const body = await request.json();
    if (!body?.reason_code || !body?.description) {
      return HttpResponse.json(apiError(400, "Validation failed"), { status: 400 });
    }
    const result = applyMockEnforcement(params.userId, "SUSPEND", body, actor.id);
    if (result?.conflict) {
      return HttpResponse.json(apiError(409, "Enforcement already active"), { status: 409 });
    }
    return HttpResponse.json(
      apiSuccess(200, "User suspended successfully", {
        enforcement_id: result.enforcement.enforcement_id,
        user_id: params.userId,
        reason_code: body.reason_code,
        status: "ACTIVE",
        expires_at: body.expires_at || null,
        enforced_by: actor.id,
        created_at: result.enforcement.created_at,
        outbox_event_id: crypto.randomUUID(),
      }),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/users/:userId/ban", async ({ request, params }) => {
    await delay(400);
    const actor = getActorFromRequest(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const body = await request.json();
    if (!body?.reason_code || !body?.description) {
      return HttpResponse.json(apiError(400, "Validation failed"), { status: 400 });
    }
    const result = applyMockEnforcement(params.userId, "BAN", body, actor.id);
    if (result?.conflict) {
      return HttpResponse.json(apiError(409, "Enforcement already active"), { status: 409 });
    }
    return HttpResponse.json(
      apiSuccess(200, "User banned successfully", {
        enforcement_id: result.enforcement.enforcement_id,
        user_id: params.userId,
        reason_code: body.reason_code,
        status: "ACTIVE",
        enforced_by: actor.id,
        created_at: result.enforcement.created_at,
        outbox_event_id: crypto.randomUUID(),
      }),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/users/:userId/restrict", async ({ request, params }) => {
    await delay(400);
    const actor = getActorFromRequest(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const body = await request.json();
    if (!body?.reason_code || !body?.description) {
      return HttpResponse.json(apiError(400, "Validation failed"), { status: 400 });
    }
    const result = applyMockEnforcement(params.userId, "RESTRICT", body, actor.id);
    if (result?.conflict) {
      return HttpResponse.json(apiError(409, "Enforcement already active"), { status: 409 });
    }
    return HttpResponse.json(
      apiSuccess(200, "User restricted successfully", {
        enforcement_id: result.enforcement.enforcement_id,
        user_id: params.userId,
        reason_code: body.reason_code,
        status: "ACTIVE",
        enforced_by: actor.id,
        created_at: result.enforcement.created_at,
        outbox_event_id: crypto.randomUUID(),
      }),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/user-enforcements/:enforcementId/revoke", async ({ request, params }) => {
    await delay(350);
    const actor = getActorFromRequest(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const body = await request.json().catch(() => ({}));
    const revoked = revokeMockEnforcement(params.enforcementId, actor.id, body);
    if (!revoked) {
      return HttpResponse.json(apiError(404, "Enforcement not found"), { status: 404 });
    }
    return HttpResponse.json(
      apiSuccess(200, "User enforcement revoked successfully", {
        enforcement_id: params.enforcementId,
        user_id: revoked.user_id,
        action_type: revoked.action_type,
        status: "REVOKED",
        revoked_by: actor.id,
        updated_at: revoked.updated_at,
        outbox_event_id: crypto.randomUUID(),
      }),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/admin-sessions/:sessionId/revoke", async ({ request, params }) => {
    await delay(350);
    const actor = getActorFromRequest(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const body = await request.json().catch(() => ({}));
    const revokeAll = Boolean(body?.revoke_all_sessions);
    return HttpResponse.json(
      apiSuccess(200, "Admin session revoked successfully", {
        target_admin_user_id: actor.id,
        session_id: params.sessionId,
        revoked_session_count: revokeAll ? 3 : 1,
        revoke_all_sessions: revokeAll,
      }),
      { status: 200 },
    );
  }),
];
