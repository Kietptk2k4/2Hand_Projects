import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import { apiError, apiSuccess } from "../utils/response";

const ADMIN_ROLES = ["ADMIN"];
const ADMIN_PERMISSIONS = [
  "ADMIN_ACCESS",
  "USER_INVESTIGATION_READ",
  "USER_ENFORCEMENT_READ",
  "USER_ENFORCEMENT_REVOKE",
  "ADMIN_SESSION_REVOKE",
  "USER_SUSPEND",
  "USER_BAN",
  "USER_RESTRICT",
  "PRODUCT_REMOVE",
  "PRODUCT_RESTORE",
  "PRODUCT_MODERATION_READ",
  "REVIEW_HIDE",
  "REVIEW_REMOVE",
  "REVIEW_RESTORE",
  "SHOP_SUSPEND",
  "SHOP_CLOSE",
  "SHOP_RESTORE",
  "POST_MODERATE",
  "POST_RESTORE",
  "COMMENT_MODERATE",
  "COMMENT_RESTORE",
  "ADMIN_AUDIT_READ",
  "ADMIN_AUDIT_VIEW",
  "SYSTEM_CONFIG_VIEW",
  "SYSTEM_CONFIG_UPDATE",
  "SYSTEM_ANNOUNCEMENT_CREATE",
  "SYSTEM_ANNOUNCEMENT_UPDATE",
  "SYSTEM_ANNOUNCEMENT_PUBLISH",
  "SYSTEM_ANNOUNCEMENT_CANCEL",
  "ORDER_SUPPORT_READ",
  "PAYMENT_SUPPORT_READ",
  "SHIPMENT_SUPPORT_READ",
  "SHIPMENT_SUPPORT_WRITE",
  "WEBHOOK_SUPPORT_READ",
  "REFUND_SUPPORT_READ",
  "REFUND_SUPPORT_APPROVE",
  "CATALOG_READ",
  "CATALOG_WRITE",
  "ASSIGN_ROLE",
];

function getUserByToken(req) {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  const user = mockUsers.find((item) => token.includes(item.id));
  return user || null;
}

export const adminAuthHandlers = [
  http.post("*/api/v1/auth/admin/login", async ({ request }) => {
    await delay(500);
    const body = await request.json();
    const user = mockUsers.find((item) => item.email === body?.email);

    if (!user || user.password !== body?.password) {
      return HttpResponse.json(apiError(401, "Email hoac mat khau khong chinh xac."), { status: 401 });
    }

    if (user.status === "SUSPENDED") {
      return HttpResponse.json(
        apiError(403, "Tai khoan khong co quyen truy cap admin portal hoac da bi dinh chi."),
        { status: 403 },
      );
    }

    if (!user.is_admin) {
      return HttpResponse.json(
        apiError(403, "Tai khoan khong co quyen truy cap admin portal."),
        { status: 403 },
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Dang nhap admin thanh cong.", {
        access_token: `mock-access-${user.id}`,
        refresh_token: `mock-refresh-${user.id}`,
        expires_in: 900,
        user: {
          id: user.id,
          email: user.email,
          status: user.status,
        },
        roles: ADMIN_ROLES,
        permissions: ADMIN_PERMISSIONS,
      }),
      { status: 200 },
    );
  }),

  http.post("*/api/v1/auth/admin/logout", async ({ request }) => {
    await delay(250);
    const actor = getUserByToken(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Unauthorized"), { status: 401 });
    }

    const body = await request.json();
    if (!body?.refresh_token) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "refresh_token", reason: "REQUIRED" }]),
        { status: 400 },
      );
    }

    return HttpResponse.json(apiSuccess(200, "Dang xuat admin thanh cong.", null), { status: 200 });
  }),

  http.post("*/api/v1/auth/admin/token/refresh", async ({ request }) => {
    await delay(350);
    const body = await request.json();
    const refreshToken = body?.refresh_token;

    if (!refreshToken || !refreshToken.startsWith("mock-refresh-")) {
      return HttpResponse.json(
        apiError(401, "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai."),
        { status: 401 },
      );
    }

    const userId = refreshToken.replace("mock-refresh-", "");
    const user = mockUsers.find((item) => item.id === userId);
    if (!user?.is_admin) {
      return HttpResponse.json(
        apiError(403, "Tai khoan khong co quyen truy cap admin portal."),
        { status: 403 },
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Lam moi access token admin thanh cong.", {
        access_token: `mock-access-${userId}`,
        expires_in: 900,
      }),
      { status: 200 },
    );
  }),
];
