import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchOrderTrackStatus(orderId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/orders/${orderId}/status`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
