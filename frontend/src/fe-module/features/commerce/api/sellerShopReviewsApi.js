import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerShopReviews({ page, limit, rating, status }) {
  try {
    const params = { page, limit, status: status || "VISIBLE" };
    if (rating != null && rating !== "") {
      params.rating = rating;
    }

    const response = await commerceApiClient.get("/commerce/api/v1/seller/reviews", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function postReplyToReview(reviewId, content) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/reviews/${reviewId}/reply`,
      { content },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
