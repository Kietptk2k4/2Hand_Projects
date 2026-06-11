import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchShopReviews({
  shopId,
  page = 1,
  limit = 20,
  sort = "NEWEST",
  rating,
} = {}) {
  try {
    const params = { page, limit, sort };
    if (rating != null) {
      params.rating = rating;
    }

    const response = await commerceApiClient.get(
      `/commerce/api/v1/shops/${shopId}/reviews`,
      { params }
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
