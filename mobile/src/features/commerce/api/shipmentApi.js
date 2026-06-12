import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchShipmentDetail(shipmentId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/shipments/${shipmentId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchShipmentTracking(shipmentId) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/shipments/${shipmentId}/tracking`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
