import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/** GET /commerce/api/v1/admin/shops */
export async function fetchAdminShopList({ page, limit, status, q, sort }) {
  try {
    const params = { page, limit, sort: sort || "NEWEST" };
    if (status) params.status = status;
    if (q) params.q = q;

    const response = await commerceApiClient.get("/commerce/api/v1/admin/shops", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

/** GET /commerce/api/v1/admin/shops/{shopId} */
export async function fetchAdminShopDetail(shopId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/admin/shops/${shopId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
