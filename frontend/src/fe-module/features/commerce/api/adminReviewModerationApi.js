import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/** GET /commerce/api/v1/admin/reviews */
export async function fetchAdminReviewList({ page, limit, status, rating, q, sort }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (rating != null && rating !== "") params.rating = rating;
    if (q) params.q = q;
    if (sort) params.sort = sort;

    const response = await commerceApiClient.get("/commerce/api/v1/admin/reviews", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

/** GET /commerce/api/v1/admin/reviews/{reviewId} */
export async function fetchAdminReviewDetail(reviewId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/admin/reviews/${reviewId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
