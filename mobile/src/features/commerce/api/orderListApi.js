import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchOrderList({ page = 1, limit = 10, status } = {}) {
  try {
    const params = { page, limit };
    if (status) {
      params.status = status;
    }

    const response = await commerceApiClient.get("/commerce/api/v1/orders", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
