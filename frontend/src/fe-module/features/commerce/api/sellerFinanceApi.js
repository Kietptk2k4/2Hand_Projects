import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerRevenueSummary({ from, to } = {}) {
  try {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;

    const response = await commerceApiClient.get("/commerce/api/v1/seller/finance/summary", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerLedger({ page = 1, limit = 20 } = {}) {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/seller/finance/ledger", {
      params: { page, limit },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerRevenueTrend({ from, to, granularity = "DAY" } = {}) {
  try {
    const params = { granularity };
    if (from) params.from = from;
    if (to) params.to = to;

    const response = await commerceApiClient.get("/commerce/api/v1/seller/finance/revenue-trend", {
      params,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
