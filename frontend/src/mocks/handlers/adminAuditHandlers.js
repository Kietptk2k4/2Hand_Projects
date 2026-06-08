import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import { getMockAdminActionLogDetail, getMockAdminActionLogs } from "../data/adminAuditData";
import { apiError, apiSuccess } from "../utils/response";

function getActorFromRequest(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  return mockUsers.find((item) => token.includes(item.id)) || null;
}

function isAdminActor(actor) {
  return Boolean(actor?.is_admin);
}

export const adminAuditHandlers = [
  http.get("*/admin/api/v1/admin-action-logs", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError("ADMIN-401", "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError("ADMIN-403", "Missing permission: ADMIN_AUDIT_VIEW"), { status: 403 });
    }

    const url = new URL(request.url);
    const data = getMockAdminActionLogs({
      admin_id: url.searchParams.get("admin_id") || undefined,
      action: url.searchParams.get("action") || undefined,
      target_type: url.searchParams.get("target_type") || undefined,
      target_id: url.searchParams.get("target_id") || undefined,
      status: url.searchParams.get("status") || undefined,
      from: url.searchParams.get("from") || undefined,
      to: url.searchParams.get("to") || undefined,
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });

    return HttpResponse.json(
      apiSuccess(200, "Admin action logs retrieved successfully", data),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/admin-action-logs/:logId", async ({ request, params }) => {
    await delay(250);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError("ADMIN-401", "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError("ADMIN-403", "Missing permission: ADMIN_AUDIT_VIEW"), { status: 403 });
    }

    const detail = getMockAdminActionLogDetail(params.logId);
    if (!detail) {
      return HttpResponse.json(apiError("ADMIN-404", "Admin action log not found"), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Admin action log retrieved successfully", detail),
      { status: 200 },
    );
  }),
];