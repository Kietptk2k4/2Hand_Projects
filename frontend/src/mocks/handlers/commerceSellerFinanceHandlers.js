import { delay, http, HttpResponse } from "msw";
import {
  buildSellerRevenueSummary,
  buildSellerRevenueTrend,
  listSellerLedger,
} from "../data/commerceSellerFinanceData";
import {
  cancelSellerPayoutRequest,
  createSellerPayoutAccount,
  createSellerPayoutRequest,
  listSellerPayoutAccounts,
  listSellerPayoutRequests,
} from "../data/commerceSellerPayoutData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("COMMERCE-401", "Authentication required."), {
        status: 401,
      }),
    };
  }
  return { user };
}

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceSellerFinanceHandlers = [
  http.get("*/commerce/api/v1/seller/finance/summary", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = buildSellerRevenueSummary(auth.user.id);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay tong hop doanh thu seller thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/seller/finance/revenue-trend", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = buildSellerRevenueTrend(auth.user.id, {
      granularity: url.searchParams.get("granularity") || "DAY",
    });
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay bieu do doanh thu seller thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/seller/finance/ledger", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = listSellerLedger(auth.user.id, {
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay lich su so cai seller thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/seller/finance/payout-accounts", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = listSellerPayoutAccounts(auth.user.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Lay danh sach tai khoan rut tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/seller/finance/payout-accounts", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = createSellerPayoutAccount(auth.user.id, body);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(201, "Tao tai khoan rut tien thanh cong.", result.data), { status: 201 });
  }),

  http.get("*/commerce/api/v1/seller/finance/payout-requests", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = listSellerPayoutRequests(auth.user.id, {
      status: url.searchParams.get("status") || undefined,
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Lay danh sach yeu cau rut tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/seller/finance/payout-requests", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = createSellerPayoutRequest(auth.user.id, body);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(201, "Tao yeu cau rut tien thanh cong.", result.data), { status: 201 });
  }),

  http.post("*/commerce/api/v1/seller/finance/payout-requests/:id/cancel", async ({ request, params }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = cancelSellerPayoutRequest(auth.user.id, params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Huy yeu cau rut tien thanh cong.", result.data));
  }),
];
