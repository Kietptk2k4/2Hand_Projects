import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchProductDetail(productId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/products/${productId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

/** Returns null when product is missing or unavailable (404) instead of throwing. */
export async function fetchProductDetailIfAvailable(productId) {
  if (!productId) return null;

  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/products/${productId}`, {
      validateStatus: (status) => status === 200 || status === 404,
    });

    if (response.status === 404) {
      return null;
    }

    return unwrapResponse(response);
  } catch (error) {
    const mapped = mapAxiosError(error);
    if (mapped.code === 404 || mapped.code === "COMMERCE-404-PRODUCT") {
      return null;
    }
    throw mapped;
  }
}
