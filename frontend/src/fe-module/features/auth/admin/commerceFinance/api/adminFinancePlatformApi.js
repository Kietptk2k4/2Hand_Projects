import { adminApiClient } from "../../../../../services/http/adminApiClient";
import { mapAxiosError, unwrapResponse } from "../../../../commerce/api/commerceApiResponse";

export async function fetchAdminPlatformFinanceSummary({ from, to } = {}) {
  try {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get("/admin/api/v1/finance/platform/summary", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminPlatformRevenueTrend({ from, to, granularity = "DAY" } = {}) {
  try {
    const params = { granularity };
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get("/admin/api/v1/finance/platform/revenue-trend", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminPlatformCodPipeline() {
  try {
    const response = await adminApiClient.get("/admin/api/v1/finance/platform/cod-pipeline");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminPlatformTopSellers({ from, to, limit = 10 } = {}) {
  try {
    const params = { limit };
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get("/admin/api/v1/finance/platform/top-sellers", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminPlatformPayoutOverview({ from, to } = {}) {
  try {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get("/admin/api/v1/finance/platform/payout-overview", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminSellerFinanceSummary(sellerId, { from, to } = {}) {
  try {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get(`/admin/api/v1/finance/sellers/${sellerId}/summary`, { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminSellerFinanceLedger(sellerId, { page = 1, limit = 20 } = {}) {
  try {
    const response = await adminApiClient.get(`/admin/api/v1/finance/sellers/${sellerId}/ledger`, {
      params: { page, limit },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
