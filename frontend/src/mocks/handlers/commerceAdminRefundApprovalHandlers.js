import { delay, http, HttpResponse } from "msw";
import {
  confirmAdminRefundApproval,
  getAdminRefundApproval,
  listAdminRefundApprovals,
  rejectAdminRefundApproval,
} from "../data/commerceAdminRefundApprovalData";
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

export const commerceAdminRefundApprovalHandlers = [
  http.get("*/admin/api/v1/refund-approvals", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = listAdminRefundApprovals({
      status: url.searchParams.get("status") || undefined,
      q: url.searchParams.get("q") || undefined,
      requested_by: url.searchParams.get("requested_by") || undefined,
      payment_method: url.searchParams.get("payment_method") || undefined,
      from: url.searchParams.get("from") || undefined,
      to: url.searchParams.get("to") || undefined,
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    return HttpResponse.json(apiSuccess(200, "Refund approval queue retrieved successfully", result.data));
  }),

  http.get("*/admin/api/v1/refund-approvals/:id", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = getAdminRefundApproval(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Refund approval detail retrieved successfully", result.data));
  }),

  http.post("*/admin/api/v1/refund-approvals/:id/confirm", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = confirmAdminRefundApproval(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Refund confirmed successfully", result.data));
  }),

  http.post("*/admin/api/v1/refund-approvals/:id/reject", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json().catch(() => ({}));
    const result = rejectAdminRefundApproval(params.id, body.admin_note || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Refund request rejected successfully", result.data));
  }),

  http.get("*/commerce/api/v1/admin/refund-approvals", async ({ request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const url = new URL(request.url);
    const result = listAdminRefundApprovals({
      status: url.searchParams.get("status") || undefined,
      q: url.searchParams.get("q") || undefined,
      requested_by: url.searchParams.get("requested_by") || undefined,
      payment_method: url.searchParams.get("payment_method") || undefined,
      from: url.searchParams.get("from") || undefined,
      to: url.searchParams.get("to") || undefined,
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
    });
    return HttpResponse.json(apiSuccess(200, "Lay danh sach duyet hoan tien thanh cong.", result.data));
  }),

  http.get("*/commerce/api/v1/admin/refund-approvals/:id", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = getAdminRefundApproval(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Lay chi tiet duyet hoan tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/admin/refund-approvals/:id/confirm", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const result = confirmAdminRefundApproval(params.id);
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Xac nhan da hoan tien thanh cong.", result.data));
  }),

  http.post("*/commerce/api/v1/admin/refund-approvals/:id/reject", async ({ request, params }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;
    const body = await request.json().catch(() => ({}));
    const result = rejectAdminRefundApproval(params.id, body.admin_note || "");
    if (result.error) return mapError(result);
    return HttpResponse.json(apiSuccess(200, "Tu choi yeu cau hoan tien thanh cong.", result.data));
  }),
];
