import { adminApiClient } from "../../../../../services/http/adminApiClient";
import { mapAxiosError, unwrapResponse } from "../../../../commerce/api/commerceApiResponse";

export async function fetchAdminPayoutQueue({ status, page = 1, limit = 20 } = {}) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    const response = await adminApiClient.get("/admin/api/v1/finance/payout-requests", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function approveAdminPayoutRequest(payoutRequestId) {
  try {
    const response = await adminApiClient.post(
      `/admin/api/v1/finance/payout-requests/${payoutRequestId}/approve`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function rejectAdminPayoutRequest(payoutRequestId, adminNote = "") {
  try {
    const response = await adminApiClient.post(
      `/admin/api/v1/finance/payout-requests/${payoutRequestId}/reject`,
      { admin_note: adminNote },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function markAdminPayoutRequestPaid(payoutRequestId, bankTransferRef) {
  try {
    const response = await adminApiClient.post(
      `/admin/api/v1/finance/payout-requests/${payoutRequestId}/mark-paid`,
      { bank_transfer_ref: bankTransferRef },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
