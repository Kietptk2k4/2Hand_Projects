import { delay, http, HttpResponse } from "msw";
import {
  getAdminProductDetail,
  listAdminProductsForAdmin,
  userHasAdminProductAccess,
  validateAdminProductListQuery,
} from "../data/commerceAdminProductRemovalData";
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
  if (!userHasAdminProductAccess(user)) {
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

export const commerceAdminProductRemovalHandlers = [
  /**
   * GET /commerce/api/v1/admin/products
   */
  http.get("*/commerce/api/v1/admin/products", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const denied = requireAdmin(auth.user);
    if (denied) return denied.error;

    const url = new URL(request.url);

    const validated = validateAdminProductListQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      status: url.searchParams.get("status") || undefined,
      q: url.searchParams.get("q") || undefined,
      sort: url.searchParams.get("sort") || undefined,
    });

    if (validated.error) return mapError(validated);

    const result = listAdminProductsForAdmin(validated);
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach san pham admin thanh cong.", result.data),
      { status: 200 },
    );
  }),

  /**
   * GET /commerce/api/v1/admin/products/:productId
   */
  http.get("*/commerce/api/v1/admin/products/:productId", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const denied = requireAdmin(auth.user);
    if (denied) return denied.error;

    const result = getAdminProductDetail(params.productId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet san pham kiem duyet thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
