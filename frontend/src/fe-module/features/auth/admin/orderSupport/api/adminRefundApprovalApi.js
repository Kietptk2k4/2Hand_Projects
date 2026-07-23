import { adminApiClient } from "../../../../../services/http/adminApiClient";
import { mapAxiosError, unwrapResponse } from "../../../../commerce/api/commerceApiResponse";

export async function fetchAdminRefundApprovals({
  status,
  q,
  requested_by,
  payment_method,
  from,
  to,
  page = 1,
  limit = 20,
} = {}) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (q) params.q = q;
    if (requested_by) params.requested_by = requested_by;
    if (payment_method) params.payment_method = payment_method;
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await adminApiClient.get("/admin/api/v1/refund-approvals", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminRefundApprovalDetail(refundRequestId) {
  try {
    const response = await adminApiClient.get(`/admin/api/v1/refund-approvals/${refundRequestId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function confirmAdminRefundApproval(refundRequestId, adminNote = "") {
  try {
    const response = await adminApiClient.post(
      `/admin/api/v1/refund-approvals/${refundRequestId}/confirm`,
      { admin_note: adminNote },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function rejectAdminRefundApproval(refundRequestId, adminNote = "") {
  try {
    const response = await adminApiClient.post(
      `/admin/api/v1/refund-approvals/${refundRequestId}/reject`,
      { admin_note: adminNote },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
