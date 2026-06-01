import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/**
 * FE-only list endpoint — chua co backend contract chinh thuc.
 * GET /commerce/api/v1/admin/shops
 */
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

export async function moderateShop(shopId, payload) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/admin/shops/${shopId}/moderate`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
