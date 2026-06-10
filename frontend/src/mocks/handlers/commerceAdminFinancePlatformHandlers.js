import { delay, http, HttpResponse } from "msw";
import {
  buildAdminSellerFinanceLedger,
  buildAdminSellerFinanceSummary,
  buildPlatformCodPipeline,
  buildPlatformFinanceSummary,
  buildPlatformRevenueTrend,
  buildPlatformTopSellers,
} from "../data/commerceAdminFinancePlatformData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("ADMIN-401", "Authentication required."), { status: 401 }),
    };
  }
  return { user };
}

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceAdminFinancePlatformHandlers = [
  http.get("*/admin/api/v1/finance/platform/summary", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = buildPlatformFinanceSummary();
    return HttpResponse.json(apiSuccess(200, "Platform finance summary retrieved.", result.data));
  }),

  http.get("*/admin/api/v1/finance/platform/revenue-trend", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = buildPlatformRevenueTrend({
      granularity: url.searchParams.get("granularity") || "DAY",
    });
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Platform revenue trend retrieved.", result.data));
  }),

  http.get("*/admin/api/v1/finance/platform/cod-pipeline", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = buildPlatformCodPipeline();
    return HttpResponse.json(apiSuccess(200, "Platform COD pipeline retrieved.", result.data));
  }),

  http.get("*/admin/api/v1/finance/platform/top-sellers", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = buildPlatformTopSellers({ limit: url.searchParams.get("limit") || "10" });
    return HttpResponse.json(apiSuccess(200, "Platform top sellers retrieved.", result.data));
  }),

  http.get("*/admin/api/v1/finance/sellers/:sellerId/summary", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = buildAdminSellerFinanceSummary(params.sellerId);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Seller finance summary retrieved.", result.data));
  }),

  http.get("*/admin/api/v1/finance/sellers/:sellerId/ledger", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = buildAdminSellerFinanceLedger(params.sellerId, {
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Seller finance ledger retrieved.", result.data));
  }),
];
