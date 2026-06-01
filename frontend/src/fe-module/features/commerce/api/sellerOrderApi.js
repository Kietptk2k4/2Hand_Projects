import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerOrderList({ page, limit, status, shipmentStatus }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (shipmentStatus) params.shipment_status = shipmentStatus;

    const response = await commerceApiClient.get("/commerce/api/v1/seller/orders", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
