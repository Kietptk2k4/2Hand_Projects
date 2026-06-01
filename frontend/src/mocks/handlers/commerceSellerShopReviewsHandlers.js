import { delay, http, HttpResponse } from "msw";
import {
  listSellerShopReviewsForUser,
  replyToSellerReview,
  validateSellerShopReviewsQuery,
} from "../data/commerceSellerShopReviewsData";
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

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceSellerShopReviewsHandlers = [
  http.get("*/commerce/api/v1/seller/reviews", async ({ request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const validated = validateSellerShopReviewsQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      rating: url.searchParams.get("rating") || undefined,
      status: url.searchParams.get("status") || "VISIBLE",
    });

    if (validated.error) return mapError(validated);

    const result = listSellerShopReviewsForUser(auth.user.id, validated);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach danh gia shop thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/reviews/:reviewId/reply", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = replyToSellerReview(auth.user.id, params.reviewId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Phan hoi danh gia thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
