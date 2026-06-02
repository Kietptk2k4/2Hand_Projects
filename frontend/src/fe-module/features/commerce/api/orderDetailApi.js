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

export async function cancelOrder(orderId, reason) {
  try {
    const payload = reason?.trim() ? { reason: reason.trim() } : {};
    const response = await commerceApiClient.post(
      `/commerce/api/v1/orders/${orderId}/cancel`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function confirmOrderReceived(orderId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/orders/${orderId}/confirm-received`,
      {},
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
