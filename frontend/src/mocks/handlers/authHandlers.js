import { delay, http, HttpResponse } from "msw";
import {
  DEVICE_RATE_LIMIT_EMAIL,
  mockLoginHistoryByUserId,
  mockSessionsByUserId,
  mockUsers,
} from "../data/authData";
import { apiError, apiSuccess } from "../utils/response";

function getUserByToken(req) {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  if (token.includes("expired-access")) return null;
  const user = mockUsers.find((item) => token.includes(item.id));
  return user || null;
}

export const authHandlers = [
  http.post("*/api/v1/auth/register", async ({ request }) => {
    await delay(500);
    const body = await request.json();

    if (!body?.email || !body?.password || !body?.confirm_password) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "email", reason: "Missing required fields" }]),
        { status: 400 }
      );
    }

    if (body.password !== body.confirm_password) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "confirm_password", reason: "Password mismatch" }]),
        { status: 400 }
      );
    }

    if (mockUsers.some((u) => u.email === body.email)) {
      return HttpResponse.json(apiError(409, "Email da duoc su dung."), { status: 409 });
    }

    if (body.email === DEVICE_RATE_LIMIT_EMAIL) {
      return HttpResponse.json(apiError(429, "Ban thao tac qua nhanh, vui long thu lai sau."), {
        status: 429
      });
    }

    return HttpResponse.json(
      apiSuccess(201, "Dang ky thanh cong. Vui long kiem tra email de xac thuc.", {
        user_id: crypto.randomUUID(),
        email: body.email,
        status: "PENDING_VERIFICATION"
      }),
      { status: 201 }
    );
  }),

  http.post("*/api/v1/auth/login", async ({ request }) => {
    await delay(500);
    const body = await request.json();
    const user = mockUsers.find((item) => item.email === body?.email);

    if (!user || user.password !== body?.password) {
      return HttpResponse.json(apiError(401, "Email hoac mat khau khong chinh xac."), { status: 401 });
    }

    if (body?.email === DEVICE_RATE_LIMIT_EMAIL) {
      return HttpResponse.json(apiError(429, "Ban thu dang nhap qua nhieu lan. Vui long thu lai sau."), {
        status: 429
      });
    }

    return HttpResponse.json(
      apiSuccess(200, "Dang nhap thanh cong.", {
        access_token: `mock-access-${user.id}`,
        refresh_token: `mock-refresh-${user.id}`,
        expires_in: 1800,
        user: {
          id: user.id,
          email: user.email,
          status: user.status
        }
      }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/auth/refresh", async ({ request }) => {
    await delay(350);
    const body = await request.json();
    const refreshToken = body?.refresh_token;

    if (refreshToken === "mock-refresh-expired") {
      return HttpResponse.json(
        apiError(401, "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai."),
        { status: 401 }
      );
    }

    if (!refreshToken || !refreshToken.startsWith("mock-refresh-")) {
      return HttpResponse.json(
        apiError(401, "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai."),
        { status: 401 }
      );
    }

    const userId = refreshToken.replace("mock-refresh-", "");
    return HttpResponse.json(
      apiSuccess(200, "Lam moi access token thanh cong.", {
        access_token: `mock-access-${userId}`,
        expires_in: 1800
      }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/auth/logout", async () => {
    await delay(250);
    return HttpResponse.json(apiSuccess(200, "Dang xuat thanh cong.", null), { status: 200 });
  }),

  http.post("*/api/v1/auth/forgot-password", async ({ request }) => {
    await delay(400);
    const body = await request.json();

    if (!body?.email) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "email", reason: "Email is required" }]),
        { status: 400 }
      );
    }

    if (body.email === DEVICE_RATE_LIMIT_EMAIL) {
      return HttpResponse.json(apiError(429, "Ban thao tac qua nhanh, vui long thu lai sau."), {
        status: 429
      });
    }

    return HttpResponse.json(
      apiSuccess(200, "Neu email hop le, chung toi da gui huong dan dat lai mat khau.", null),
      { status: 200 }
    );
  }),

  // Mock credentials: active@2hands.vn / Password123!
  http.post("*/api/v1/auth/change-password", async ({ request }) => {
    await delay(450);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const body = await request.json();
    if (!body?.current_password || !body?.new_password || !body?.confirm_new_password) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "current_password", reason: "Required fields missing" }]),
        { status: 400 }
      );
    }
    if (body.new_password !== body.confirm_new_password) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "confirm_new_password", reason: "Password mismatch" }]),
        { status: 400 }
      );
    }
    if (body.current_password !== user.password) {
      return HttpResponse.json(
        apiError(400, "Mat khau khong chinh xac.", [{ field: "password", reason: "INVALID_CREDENTIAL" }]),
        { status: 400 }
      );
    }
    if (body.new_password === body.current_password) {
      return HttpResponse.json(
        apiError(400, "Mat khau moi phai khac mat khau hien tai.", [
          { field: "new_password", reason: "SAME_AS_CURRENT" },
        ]),
        { status: 400 }
      );
    }

    user.password = body.new_password;
    return HttpResponse.json(apiSuccess(200, "Doi mat khau thanh cong.", null), { status: 200 });
  }),

  http.post("*/api/v1/auth/verify-email", async ({ request }) => {
    await delay(400);
    const body = await request.json();
    if (!body?.token || body.token === "expired-token") {
      return HttpResponse.json(
        apiError(400, "Token xac thuc khong hop le hoac da het han.", [
          { field: "token", reason: "INVALID_OR_EXPIRED" }
        ]),
        { status: 400 }
      );
    }
    return HttpResponse.json(
      apiSuccess(200, "Xac thuc email thanh cong.", {
        user_id: crypto.randomUUID(),
        email_verified: true,
        status: "ACTIVE"
      }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/users/me", async ({ request }) => {
    await delay(300);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay thong tin tai khoan thanh cong.", {
        user: {
          id: user.id,
          email: user.email,
          status: user.status,
          email_verified: user.email_verified,
          phone: null,
          last_login_at: user.last_login_at
        },
        profile: {
          display_name: user.display_name,
          avatar_url: user.avatar_url,
          bio: user.bio,
          website: user.website,
          social_links: user.social_links,
          is_private: user.is_private
        },
        settings: {
          appearance_mode: user.appearance_mode
        }
      }),
      { status: 200 }
    );
  }),

  http.put("*/api/v1/users/me/profile", async ({ request }) => {
    await delay(300);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    if (!body?.display_name?.trim()) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "display_name", reason: "Required" }]),
        { status: 400 }
      );
    }
    if (body.website && !/^https?:\/\//i.test(body.website)) {
      return HttpResponse.json(
        apiError(400, "URL khong hop le.", [{ field: "website", reason: "INVALID_URL" }]),
        { status: 400 }
      );
    }
    user.display_name = body.display_name;
    user.bio = body.bio ?? user.bio;
    user.website = body.website ?? user.website;
    user.social_links = body.social_links ?? user.social_links;
    return HttpResponse.json(apiSuccess(200, "Cap nhat ho so thanh cong.", null), { status: 200 });
  }),

  http.post("*/api/v1/users/me/avatar/upload-url", async ({ request }) => {
    await delay(350);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    const allowed = ["image/jpeg", "image/png", "image/webp"];
    if (!allowed.includes(body?.content_type)) {
      return HttpResponse.json(
        apiError(400, "Dinh dang khong duoc ho tro.", [{ field: "content_type", reason: "INVALID" }]),
        { status: 400 }
      );
    }
    if (!body?.file_size_bytes || body.file_size_bytes > 5_242_880) {
      return HttpResponse.json(
        apiError(400, "Tep vuot qua 5MB.", [{ field: "file_size_bytes", reason: "TOO_LARGE" }]),
        { status: 400 }
      );
    }
    const objectKey = `avatars/${user.id}/${crypto.randomUUID()}.png`;
    const avatarUrl = `https://cdn.2hands.vn/${objectKey}`;
    return HttpResponse.json(
      apiSuccess(200, "Tao link upload avatar thanh cong.", {
        upload_url: `https://mock-minio.2hands.vn/upload/${objectKey}`,
        object_key: objectKey,
        avatar_url: avatarUrl,
        expires_at: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
        max_file_size_bytes: 5_242_880,
        allowed_content_types: allowed,
      }),
      { status: 200 }
    );
  }),

  http.put("https://mock-minio.2hands.vn/upload/*", async () => {
    await delay(200);
    return new HttpResponse(null, { status: 200 });
  }),

  http.patch("*/api/v1/users/me/avatar", async ({ request }) => {
    await delay(300);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    if (!body?.avatar_url) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "avatar_url", reason: "Required" }]),
        { status: 400 }
      );
    }
    user.avatar_url = body.avatar_url;
    return HttpResponse.json(apiSuccess(200, "Cap nhat avatar thanh cong.", null), { status: 200 });
  }),

  http.patch("*/api/v1/users/me/privacy", async ({ request }) => {
    await delay(250);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    user.is_private = Boolean(body?.is_private);
    return HttpResponse.json(apiSuccess(200, "Cap nhat quyen rieng tu thanh cong.", null), { status: 200 });
  }),

  http.patch("*/api/v1/users/me/settings", async ({ request }) => {
    await delay(250);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    const allowed = ["LIGHT", "DARK", "SYSTEM"];
    if (!allowed.includes(body?.appearance_mode)) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "appearance_mode", reason: "INVALID" }]),
        { status: 400 }
      );
    }
    user.appearance_mode = body.appearance_mode;
    return HttpResponse.json(
      apiSuccess(200, "Cap nhat cai dat thanh cong.", {
        appearance_mode: user.appearance_mode,
      }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/users/me/sessions", async ({ request }) => {
    await delay(300);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const sessions = mockSessionsByUserId[user.id] || [];
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach phien dang nhap thanh cong.", { sessions }),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/users/me/login-history", async ({ request }) => {
    await delay(350);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const url = new URL(request.url);
    const limit = Number.parseInt(url.searchParams.get("limit") || "20", 10);
    const offset = Number.parseInt(url.searchParams.get("offset") || "0", 10);

    if (Number.isNaN(limit) || limit < 1 || limit > 100) {
      return HttpResponse.json(
        apiError(400, "Limit khong hop le.", [{ field: "limit", reason: "INVALID_RANGE" }]),
        { status: 400 }
      );
    }
    if (Number.isNaN(offset) || offset < 0) {
      return HttpResponse.json(
        apiError(400, "Offset khong hop le.", [{ field: "offset", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    const all = mockLoginHistoryByUserId[user.id] || [];
    const items = all.slice(offset, offset + limit);

    return HttpResponse.json(
      apiSuccess(200, "Lay lich su dang nhap thanh cong.", { items, limit, offset }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/users/me/soft-delete", async ({ request }) => {
    await delay(400);
    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    const body = await request.json();
    if (!body?.password || body.password !== user.password) {
      return HttpResponse.json(
        apiError(400, "Mat khau khong chinh xac.", [{ field: "password", reason: "INVALID_CREDENTIAL" }]),
        { status: 400 }
      );
    }
    user.status = "DELETED";
    return HttpResponse.json(apiSuccess(200, "Xoa tai khoan thanh cong.", null), { status: 200 });
  }),
];

