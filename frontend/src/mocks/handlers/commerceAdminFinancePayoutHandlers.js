import { delay, http, HttpResponse } from "msw";
import {
  approveAdminPayoutRequest,
  listAdminPayoutRequests,
  markAdminPayoutRequestPaid,
  rejectAdminPayoutRequest,
} from "../data/commerceSellerPayoutData";
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

export const commerceAdminFinancePayoutHandlers = [
  http.get("*/admin/api/v1/finance/payout-requests", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = listAdminPayoutRequests({
      status: url.searchParams.get("status") || undefined,
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    return HttpResponse.json(apiSuccess(200, "Payout queue retrieved successfully", result.data));
  }),

  http.post("*/admin/api/v1/finance/payout-requests/:id/approve", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = approveAdminPayoutRequest(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Payout request approved successfully", result.data));
  }),

  http.post("*/admin/api/v1/finance/payout-requests/:id/reject", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json().catch(() => ({}));
    const result = rejectAdminPayoutRequest(params.id, body.admin_note || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Payout request rejected successfully", result.data));
  }),

  http.post("*/admin/api/v1/finance/payout-requests/:id/mark-paid", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = markAdminPayoutRequestPaid(params.id, body.bank_transfer_ref || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Payout request marked as paid successfully", result.data));
  }),

  http.get("*/commerce/api/v1/admin/finance/payout-requests", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = listAdminPayoutRequests({
      status: url.searchParams.get("status") || undefined,
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    return HttpResponse.json(apiSuccess(200, "Lay hang doi rut tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/admin/finance/payout-requests/:id/approve", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = approveAdminPayoutRequest(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Duyet yeu cau rut tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/admin/finance/payout-requests/:id/reject", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json().catch(() => ({}));
    const result = rejectAdminPayoutRequest(params.id, body.admin_note || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Tu choi yeu cau rut tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/admin/finance/payout-requests/:id/mark-paid", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json();
    const result = markAdminPayoutRequestPaid(params.id, body.bank_transfer_ref || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Ghi nhan chuyen khoan rut tien thanh cong.", result.data));
  }),
];
