import { delay, http, HttpResponse } from "msw";
import {
  createReviewForBuyer,
  getMyReviewForProduct,
  getReviewContextForOrderItem,
  getReviewForBuyer,
  updateReviewForBuyer,
} from "../data/commerceProductReviewWriteData";
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

export const commerceProductReviewWriteHandlers = [
  http.get("*/commerce/api/v1/me/products/:productId/review", async ({ params, request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = getMyReviewForProduct(auth.user.id, params.productId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh gia cua ban thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/reviews/context", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const orderItemId = url.searchParams.get("order_item_id");
    if (!orderItemId) {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Thieu order_item_id."), {
        status: 400,
      });
    }

    const result = getReviewContextForOrderItem(auth.user.id, orderItemId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay thong tin danh gia thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/reviews/:reviewId", async ({ params, request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const reviewId = params.reviewId;
    if (reviewId === "context") {
      return HttpResponse.json(apiError("COMMERCE-404-REVIEW", "Khong tim thay danh gia."), {
        status: 404,
      });
    }

    const result = getReviewForBuyer(auth.user.id, reviewId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet danh gia thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/reviews", async ({ request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Body khong hop le."), {
        status: 400,
      });
    }

    const rating = body.rating != null ? Number(body.rating) : NaN;
    const result = createReviewForBuyer(auth.user.id, {
      order_item_id: body.order_item_id,
      rating,
      comment: body.comment,
    });

    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Tao danh gia san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/reviews/:reviewId", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const reviewId = params.reviewId;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Body khong hop le."), {
        status: 400,
      });
    }

    const patch = {};
    if (body.rating !== undefined) {
      patch.rating = Number(body.rating);
    }
    if (body.comment !== undefined) {
      patch.comment = body.comment;
    }

    const result = updateReviewForBuyer(auth.user.id, reviewId, patch);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat danh gia thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
