import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchOrderDetail(orderId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/orders/${orderId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
