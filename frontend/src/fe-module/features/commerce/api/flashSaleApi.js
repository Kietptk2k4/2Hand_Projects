import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchFlashSaleProducts({ limit = 20 } = {}) {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/products/flash-sale", {
      params: { limit },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
