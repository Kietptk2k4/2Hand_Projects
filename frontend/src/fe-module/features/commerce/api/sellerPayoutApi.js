import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerPayoutAccounts() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/seller/finance/payout-accounts");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createSellerPayoutAccount(payload) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/seller/finance/payout-accounts", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateSellerPayoutAccount(accountId, payload) {
  try {
    const response = await commerceApiClient.put(
      `/commerce/api/v1/seller/finance/payout-accounts/${accountId}`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerPayoutRequests({ status, page = 1, limit = 20 } = {}) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    const response = await commerceApiClient.get("/commerce/api/v1/seller/finance/payout-requests", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createSellerPayoutRequest(payload) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/seller/finance/payout-requests", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function cancelSellerPayoutRequest(payoutRequestId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/finance/payout-requests/${payoutRequestId}/cancel`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
