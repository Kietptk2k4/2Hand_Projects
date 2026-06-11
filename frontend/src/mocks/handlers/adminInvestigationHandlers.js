import { delay, http, HttpResponse } from "msw";
import { mockLoginHistoryByUserId, mockSessionsByUserId, mockUsers } from "../data/authData";
import {
  mockRoles,
  mockUserRoleAssignments,
} from "../data/adminRbacData";
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

function paginate(items, page, limit) {
  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / limit));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * limit;
  const slice = items.slice(start, start + limit);
  return {
    items: slice,
    pagination: {
      page: safePage,
      limit,
      total_items: totalItems,
      total_pages: totalPages,
      has_next: safePage < totalPages,
    },
  };
}

function mapSessionForAdmin(session) {
  return {
    session_id: session.session_id || session.id,
    device_id: session.device_id,
    ip_address: session.ip_address,
    user_agent: session.user_agent,
    status: session.status,
    created_at: session.created_at,
    updated_at: session.updated_at,
  };
}

function mapUserForInvestigationSearch(user) {
  return {
    user_id: user.id,
    email: user.email,
    display_name: user.display_name || user.email,
    status: user.status,
    role_codes: user.is_admin ? ["ADMIN"] : ["USER"],
  };
}

function roleCodesForUser(userId) {
  const roleId = mockUserRoleAssignments[userId];
  if (!roleId) return [];
  const role = mockRoles.find((r) => r.id === roleId);
  return role ? [role.code] : [];
}

function buildInvestigationUserListItem(user) {
  return {
    id: user.id,
    email: user.email,
    display_name: user.display_name || "",
    status: user.status,
    role_codes: roleCodesForUser(user.id),
    created_at: user.created_at || "2026-01-01T00:00:00Z",
  };
}

function sortInvestigationUsers(items, sort) {
  const sorted = [...items];
  switch (sort) {
    case "display_name":
      sorted.sort((a, b) => (a.display_name || "").localeCompare(b.display_name || ""));
      break;
    case "created_at":
      sorted.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
      break;
    case "status":
      sorted.sort((a, b) => a.status.localeCompare(b.status));
      break;
    case "email":
    default:
      sorted.sort((a, b) => a.email.localeCompare(b.email));
      break;
  }
  return sorted;
}

export const adminInvestigationHandlers = [
  http.get("*/api/v1/admin/users/investigation", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const status = url.searchParams.get("status") || "";
    const q = (url.searchParams.get("q") || "").trim().toLowerCase();
    const sort = url.searchParams.get("sort") || "created_at";
    const page = Math.max(1, Number(url.searchParams.get("page")) || 1);
    const size = Math.max(1, Math.min(50, Number(url.searchParams.get("size")) || 20));

    let items = mockUsers
      .filter((user) => user.status !== "DELETED")
      .map(buildInvestigationUserListItem);

    if (status) {
      items = items.filter((item) => item.status === status);
    }

    if (q) {
      items = items.filter((item) => item.email.toLowerCase().includes(q));
    }

    items = sortInvestigationUsers(items, sort);

    const totalItems = items.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / size));
    const safePage = Math.min(page, totalPages);
    const start = (safePage - 1) * size;
    const pageItems = items.slice(start, start + size);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach user dieu tra thanh cong.", {
        items: pageItems,
        pagination: {
          page: safePage,
          size,
          total_items: totalItems,
          total_pages: totalPages,
          has_next: safePage < totalPages,
        },
      }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/admin/users/search", async ({ request }) => {
    await delay(250);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const query = (url.searchParams.get("query") || "").trim().toLowerCase();
    const limit = Math.min(Math.max(Number(url.searchParams.get("limit") || 20), 1), 50);

    if (!query) {
      return HttpResponse.json(
        apiSuccess(200, "User investigation search completed successfully", { users: [] }),
      );
    }

    const users = mockUsers
      .filter((user) => user.status !== "DELETED")
      .filter((user) => {
        if (isValidUuid(query)) {
          return user.id.toLowerCase() === query;
        }
        return query.length >= 2 && user.email.toLowerCase().includes(query);
      })
      .slice(0, limit)
      .map(mapUserForInvestigationSearch);

    return HttpResponse.json(
      apiSuccess(200, "User investigation search completed successfully", { users }),
    );
  }),

  http.get("*/api/v1/admin/users/:userId/login-history", async ({ request, params }) => {
    await delay(350);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "userId", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    const target = getTargetUser(userId);
    if (!target) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    const url = new URL(request.url);
    const page = Number.parseInt(url.searchParams.get("page") || "1", 10);
    const limit = Number.parseInt(url.searchParams.get("limit") || "20", 10);
    const successParam = url.searchParams.get("success");
    const from = url.searchParams.get("from");
    const to = url.searchParams.get("to");

    if (Number.isNaN(page) || page < 1 || Number.isNaN(limit) || limit < 1 || limit > 100) {
      return HttpResponse.json(
        apiError(400, "Pagination khong hop le.", [{ field: "page", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    if (successParam && successParam !== "true" && successParam !== "false") {
      return HttpResponse.json(
        apiError(400, "Filter success khong hop le.", [{ field: "success", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    if (from && to && new Date(from) > new Date(to)) {
      return HttpResponse.json(
        apiError(400, "Khoang thoi gian khong hop le.", [{ field: "to", reason: "BEFORE_FROM" }]),
        { status: 400 }
      );
    }

    let items = [...(mockLoginHistoryByUserId[userId] || [])];

    if (successParam === "true") {
      items = items.filter((item) => item.success === true);
    } else if (successParam === "false") {
      items = items.filter((item) => item.success === false);
    }

    if (from) {
      const fromTime = new Date(from).getTime();
      items = items.filter((item) => new Date(item.created_at).getTime() >= fromTime);
    }

    if (to) {
      const toTime = new Date(to).getTime();
      items = items.filter((item) => new Date(item.created_at).getTime() <= toTime);
    }

    const { items: pageItems, pagination } = paginate(items, page, limit);

    return HttpResponse.json(
      apiSuccess(200, "Lay lich su dang nhap thanh cong.", {
        user_id: userId,
        items: pageItems,
        pagination,
      }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/admin/users/:userId/sessions", async ({ request, params }) => {
    await delay(350);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "userId", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    const target = getTargetUser(userId);
    if (!target) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    const url = new URL(request.url);
    const page = Number.parseInt(url.searchParams.get("page") || "1", 10);
    const limit = Number.parseInt(url.searchParams.get("limit") || "20", 10);
    const statusFilter = url.searchParams.get("status") || "ACTIVE";
    const allowedStatuses = ["ACTIVE", "LOGGED_OUT", "REVOKED", "EXPIRED", "ALL"];

    if (Number.isNaN(page) || page < 1 || Number.isNaN(limit) || limit < 1 || limit > 50) {
      return HttpResponse.json(
        apiError(400, "Pagination khong hop le.", [{ field: "limit", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    if (!allowedStatuses.includes(statusFilter)) {
      return HttpResponse.json(
        apiError(400, "Status khong hop le.", [{ field: "status", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    let sessions = (mockSessionsByUserId[userId] || []).map(mapSessionForAdmin);

    if (statusFilter !== "ALL") {
      sessions = sessions.filter((s) => s.status === statusFilter);
    }

    const { items: pageSessions, pagination } = paginate(sessions, page, limit);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach phien dang nhap thanh cong.", {
        user_id: userId,
        sessions: pageSessions,
        pagination,
      }),
      { status: 200 }
    );
  }),
];
