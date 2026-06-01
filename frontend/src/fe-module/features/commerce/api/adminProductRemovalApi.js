import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/**
 * FE-only GET list — chua co backend contract chinh thuc.
 * GET /commerce/api/v1/admin/products
 */
export async function fetchAdminProductList({ page, limit, status, q }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (q) params.q = q;

    const response = await commerceApiClient.get("/commerce/api/v1/admin/products", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function removeProductByAdmin(productId, payload) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/admin/products/${productId}/remove`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
