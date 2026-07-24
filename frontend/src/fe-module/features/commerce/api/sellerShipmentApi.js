import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerShipmentList({ page, limit, status, q }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (q) params.q = q;

    const response = await commerceApiClient.get("/commerce/api/v1/seller/shipments", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerShipmentDetail(shipmentId) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/seller/shipments/${shipmentId}`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createSellerShipment(payload) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/seller/shipments", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateSellerShipment(shipmentId, payload) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/seller/shipments/${shipmentId}`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function cancelSellerShipment(shipmentId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/shipments/${shipmentId}/cancel`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchGhnPrintLabel(shipmentId, format = "a5") {
  try {
    const params = {};
    if (format) params.format = format;

    const response = await commerceApiClient.get(
      `/commerce/api/v1/seller/shipments/${shipmentId}/ghn/print-label`,
      { params },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
