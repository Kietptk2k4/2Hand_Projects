import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchPublicShopByUser(userId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/users/${userId}/shop`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
