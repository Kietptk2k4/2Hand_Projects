import { delay, http, HttpResponse } from "msw";
import { DEVICE_RATE_LIMIT_EMAIL, mockUsers } from "../data/authData";
import { apiError, apiSuccess } from "../utils/response";

function getUserByToken(req) {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
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

  http.post("*/api/v1/auth/change-password", async ({ request }) => {
    await delay(450);
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

  http.put("*/api/v1/users/me/profile", async () => {
    await delay(300);
    return HttpResponse.json(apiSuccess(200, "Cap nhat ho so thanh cong.", null), { status: 200 });
  }),

  http.patch("*/api/v1/users/me/avatar", async () => {
    await delay(300);
    return HttpResponse.json(apiSuccess(200, "Cap nhat avatar thanh cong.", null), { status: 200 });
  }),

  http.patch("*/api/v1/users/me/privacy", async () => {
    await delay(250);
    return HttpResponse.json(apiSuccess(200, "Cap nhat quyen rieng tu thanh cong.", null), { status: 200 });
  }),

  http.patch("*/api/v1/users/me/settings", async ({ request }) => {
    await delay(250);
    const body = await request.json();
    return HttpResponse.json(
      apiSuccess(200, "Cap nhat cai dat thanh cong.", {
        appearance_mode: body?.appearance_mode || "SYSTEM"
      }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/users/me/soft-delete", async () => {
    await delay(400);
    return HttpResponse.json(apiSuccess(200, "Xoa tai khoan thanh cong.", null), { status: 200 });
  })
];

