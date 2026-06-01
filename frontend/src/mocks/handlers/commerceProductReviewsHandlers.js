import { delay, http, HttpResponse } from "msw";
import {
  buildProductReviewsResponse,
  isProductVisibleForReviews,
  VALID_SORTS,
} from "../data/commerceProductReviewsData";
import { isValidProductId } from "../data/commerceProductDetailData";
import { apiError, apiSuccess } from "../utils/response";

function parsePagination(url) {
  const pageParam = url.searchParams.get("page");
  const limitParam = url.searchParams.get("limit");

  const page = pageParam === null || pageParam === "" ? 1 : Number(pageParam);
  const limit = limitParam === null || limitParam === "" ? 20 : Number(limitParam);

  if (!Number.isInteger(page) || page < 1) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-PAGINATION", "Tham so phan trang khong hop le."),
        { status: 400 }
      ),
    };
  }

  if (!Number.isInteger(limit) || limit < 1 || limit > 50) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-PAGINATION", "Tham so phan trang khong hop le."),
        { status: 400 }
      ),
    };
  }

  return { page, limit };
}

function parseRating(url) {
  const param = url.searchParams.get("rating");
  if (param === null || param === "") return { rating: null };

  const rating = Number(param);
  if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-RATING", "Tham so rating khong hop le."),
        { status: 400 }
      ),
    };
  }

  return { rating };
}

function parseSort(url) {
  const sort = url.searchParams.get("sort") || "NEWEST";
  if (!VALID_SORTS.includes(sort)) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "Tham so sort khong hop le."),
        { status: 400 }
      ),
    };
  }
  return { sort };
}

export const commerceProductReviewsHandlers = [
  http.get("*/commerce/api/v1/products/:productId/reviews", async ({ params, request }) => {
    await delay(400);

    const productId = params.productId;

    if (!isValidProductId(productId)) {
      return HttpResponse.json(apiError("COMMERCE-400", "productId khong hop le."), {
        status: 400,
      });
    }

    if (!isProductVisibleForReviews(productId)) {
      return HttpResponse.json(
        apiError("COMMERCE-404-PRODUCT", "San pham khong ton tai hoac khong kha dung."),
        { status: 404 }
      );
    }

    const url = new URL(request.url);
    const pagination = parsePagination(url);
    if (pagination.error) return pagination.error;

    const ratingResult = parseRating(url);
    if (ratingResult.error) return ratingResult.error;

    const sortResult = parseSort(url);
    if (sortResult.error) return sortResult.error;

    const data = buildProductReviewsResponse(productId, {
      page: pagination.page,
      limit: pagination.limit,
      rating: ratingResult.rating,
      sort: sortResult.sort,
    });

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach danh gia san pham thanh cong.", data),
      { status: 200 }
    );
  }),
];
