import { delay, http, HttpResponse } from "msw";
import {
  listAdminShops,
  userHasAdminShopModerationAccess,
  validateAdminShopListQuery,
} from "../data/commerceAdminShopModerationData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("COMMERCE-401", "Authentication required."), {
        status: 401,
      }),
    };
  }
  return { user };
}

function requireAdmin(user) {
  if (!userHasAdminShopModerationAccess(user)) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-403", "Ban khong co quyen truy cap."),
        { status: 403 },
      ),
    };
  }
  return null;
}

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceAdminShopModerationHandlers = [
  /**
   * FE-only GET list — chua co backend contract chinh thuc cho admin shop list.
   * GET /commerce/api/v1/admin/shops
   */
  http.get("*/commerce/api/v1/admin/shops", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const denied = requireAdmin(auth.user);
    if (denied) return denied.error;

    const url = new URL(request.url);
    const validated = validateAdminShopListQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      status: url.searchParams.get("status") || "all",
      q: url.searchParams.get("q") || undefined,
      sort: url.searchParams.get("sort") || "NEWEST",
    });

    if (validated.error) return mapError(validated);

    const result = listAdminShops(validated);
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach shop admin thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
