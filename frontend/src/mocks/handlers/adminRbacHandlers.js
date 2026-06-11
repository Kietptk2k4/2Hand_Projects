import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import {
  ADMIN_USER_ID,
  ROLE_IDS,
  mockAssignableUsers,
  mockRolePermissionsByRoleId,
  mockRoles,
  mockUserPermissionsByUserId,
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

function roleCodesForUser(userId) {
  const roleId = mockUserRoleAssignments[userId];
  if (!roleId) return [];
  const role = mockRoles.find((r) => r.id === roleId);
  return role ? [role.code] : [];
}

function buildRbacUserListItem(user) {
  return {
    id: user.id,
    email: user.email,
    display_name: user.display_name || "",
    status: user.status,
    role_codes: roleCodesForUser(user.id),
    created_at: user.created_at || "2026-01-01T00:00:00Z",
  };
}

function sortRbacUsers(items, sort) {
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

export const adminRbacHandlers = [
  http.get("*/api/v1/admin/users", async ({ request }) => {
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
    const sort = url.searchParams.get("sort") || "email";
    const page = Math.max(1, Number(url.searchParams.get("page")) || 1);
    const size = Math.max(1, Math.min(100, Number(url.searchParams.get("size")) || 20));

    let items = mockUsers
      .filter((user) => user.status !== "DELETED")
      .map(buildRbacUserListItem);

    if (status) {
      items = items.filter((item) => item.status === status);
    }

    if (q) {
      items = items.filter(
        (item) =>
          item.email.toLowerCase().includes(q) ||
          (item.display_name || "").toLowerCase().includes(q)
      );
    }

    items = sortRbacUsers(items, sort);

    const totalItems = items.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / size));
    const safePage = Math.min(page, totalPages);
    const start = (safePage - 1) * size;
    const pageItems = items.slice(start, start + size);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach user thanh cong.", {
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

  http.get("*/api/v1/admin/roles", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach role thanh cong.", { roles: mockRoles }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/admin/roles/:roleId/permissions", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const roleId = params.roleId;
    if (!isValidUuid(roleId)) {
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "roleId", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    const role = mockRoles.find((r) => r.id === roleId);
    if (!role) {
      return HttpResponse.json(apiError(404, "Khong tim thay role."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach permission cua role thanh cong.", {
        role: { id: role.id, code: role.code, name: role.name },
        permissions: mockRolePermissionsByRoleId[roleId] || [],
      }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/admin/users/:userId/permissions", async ({ request, params }) => {
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
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "userId", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    const target = mockUsers.find((u) => u.id === userId && u.status !== "DELETED");
    if (!target) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach permission cua user thanh cong.", {
        user_id: userId,
        permissions: mockUserPermissionsByUserId[userId] || [],
      }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/admin/users/:userId/roles", async ({ request, params }) => {
    await delay(400);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    const body = await request.json();

    if (!isValidUuid(userId) || !isValidUuid(body?.role_id)) {
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "role_id", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    if (userId === actor.id) {
      return HttpResponse.json(apiError(403, "Khong the gan role cho chinh minh."), { status: 403 });
    }

    const target = mockUsers.find((u) => u.id === userId && u.status !== "DELETED");
    if (!target) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    const role = mockRoles.find((r) => r.id === body.role_id);
    if (!role) {
      return HttpResponse.json(apiError(404, "Khong tim thay role."), { status: 404 });
    }

    if (mockUserRoleAssignments[userId] === body.role_id) {
      return HttpResponse.json(
        apiError(409, "Resource conflict", [{ field: "role_id", reason: "ALREADY_ASSIGNED" }]),
        { status: 409 }
      );
    }

    mockUserRoleAssignments[userId] = body.role_id;
    mockUserPermissionsByUserId[userId] = (mockRolePermissionsByRoleId[body.role_id] || []).map((p) => ({
      code: p.code,
    }));

    return HttpResponse.json(
      apiSuccess(200, "Gan role cho user thanh cong.", {
        user_id: userId,
        role_id: body.role_id,
      }),
      { status: 200 }
    );
  }),

  http.delete("*/api/v1/admin/users/:userId/roles/:roleId", async ({ request, params }) => {
    await delay(400);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const userId = params.userId;
    const roleId = params.roleId;

    if (!isValidUuid(userId) || !isValidUuid(roleId)) {
      return HttpResponse.json(
        apiError(400, "Du lieu khong hop le.", [{ field: "roleId", reason: "INVALID_FORMAT" }]),
        { status: 400 }
      );
    }

    if (userId === actor.id) {
      return HttpResponse.json(apiError(403, "Khong the thu hoi role cua chinh minh."), { status: 403 });
    }

    const target = mockUsers.find((u) => u.id === userId && u.status !== "DELETED");
    if (!target) {
      return HttpResponse.json(apiError(404, "Khong tim thay user."), { status: 404 });
    }

    const role = mockRoles.find((r) => r.id === roleId);
    if (!role) {
      return HttpResponse.json(apiError(404, "Khong tim thay role."), { status: 404 });
    }

    if (mockUserRoleAssignments[userId] !== roleId) {
      return HttpResponse.json(
        apiError(409, "Resource conflict", [{ field: "role_id", reason: "ROLE_NOT_ASSIGNED" }]),
        { status: 409 }
      );
    }

    if (roleId === ROLE_IDS.ADMIN && userId === ADMIN_USER_ID) {
      return HttpResponse.json(
        apiError(403, "Khong the thu hoi role ADMIN cuoi cung."),
        { status: 403 }
      );
    }

    delete mockUserRoleAssignments[userId];
    mockUserPermissionsByUserId[userId] = [];

    return HttpResponse.json(
      apiSuccess(200, "Thu hoi role khoi user thanh cong.", {
        user_id: userId,
        role_id: roleId,
      }),
      { status: 200 }
    );
  }),
];
