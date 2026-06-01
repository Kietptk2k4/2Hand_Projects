import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/**
 * FE-only GET list — chua co backend contract chinh thuc.
 * GET /commerce/api/v1/admin/reviews
 */
export async function fetchAdminReviewList({ page, limit, status, rating, q }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (rating != null && rating !== "") params.rating = rating;
    if (q) params.q = q;

    const response = await commerceApiClient.get("/commerce/api/v1/admin/reviews", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function moderateReview(reviewId, payload) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/admin/reviews/${reviewId}/moderate`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
