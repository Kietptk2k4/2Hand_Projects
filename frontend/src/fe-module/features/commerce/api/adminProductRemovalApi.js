import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/** GET /commerce/api/v1/admin/products */
export async function fetchAdminProductList({ page, limit, status, q, sort }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (q) params.q = q;
    if (sort) params.sort = sort;

    const response = await commerceApiClient.get("/commerce/api/v1/admin/products", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

/** GET /commerce/api/v1/admin/products/{productId} */
export async function fetchAdminProductDetail(productId) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/admin/products/${productId}`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
