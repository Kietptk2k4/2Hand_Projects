import { delay, http, HttpResponse } from "msw";
import {
  listAdminReviewsForAdmin,
  moderateAdminReview,
  userHasAdminReviewAccess,
  validateAdminReviewListQuery,
} from "../data/commerceAdminReviewModerationData";
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
  if (!userHasAdminReviewAccess(user)) {
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

export const commerceAdminReviewModerationHandlers = [
  /**
   * FE-only GET list — chua co backend contract chinh thuc cho admin review list.
   * GET /commerce/api/v1/admin/reviews
   */
  http.get("*/commerce/api/v1/admin/reviews", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const denied = requireAdmin(auth.user);
    if (denied) return denied.error;

    const url = new URL(request.url);
    const validated = validateAdminReviewListQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      status: url.searchParams.get("status") || undefined,
      rating: url.searchParams.get("rating") || undefined,
      q: url.searchParams.get("q") || undefined,
    });

    if (validated.error) return mapError(validated);

    const result = listAdminReviewsForAdmin(validated);
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach danh gia admin thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/admin/reviews/:reviewId/moderate", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const denied = requireAdmin(auth.user);
    if (denied) return denied.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminReview(params.reviewId, body, {
      isAdmin: Boolean(auth.user.is_admin),
    });

    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, result.data),
      { status: 200 },
    );
  }),
];
