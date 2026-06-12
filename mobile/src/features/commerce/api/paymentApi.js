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

function buildStatusQueryParams({ mockPaid, mockFailed, mockExpired } = {}) {
  const params = {};
  if (mockPaid) params.mockPaid = "1";
  if (mockFailed) params.mockFailed = "1";
  if (mockExpired) params.mockExpired = "1";
  return Object.keys(params).length ? params : undefined;
}

export async function fetchPaymentStatus(
  paymentId,
  { mockPaid = false, mockFailed = false, mockExpired = false } = {}
) {
  try {
    const params = buildStatusQueryParams({ mockPaid, mockFailed, mockExpired });
    const response = await commerceApiClient.get(
      `/commerce/api/v1/payments/${paymentId}/status`,
      params ? { params } : undefined
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
