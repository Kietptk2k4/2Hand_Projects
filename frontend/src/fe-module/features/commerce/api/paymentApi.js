import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function createPayOsCheckoutUrl(paymentId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/payments/${paymentId}/payos-checkout-url`
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchPaymentStatus(paymentId, { mockPaid = false } = {}) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/payments/${paymentId}/status`,
      mockPaid ? { params: { mockPaid: "1" } } : undefined
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
