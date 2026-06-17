import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchShopProducts({ shopId, page = 1, limit = 20, sort = "NEWEST" } = {}) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/shops/${shopId}/products`, {
      params: { page, limit, sort },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
