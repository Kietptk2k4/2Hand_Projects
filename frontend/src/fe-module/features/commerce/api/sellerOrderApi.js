import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function processSellerOrderItems(orderItemIds) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/seller/order-items/process", {
      order_item_ids: orderItemIds,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerOrderDetail(orderId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/seller/orders/${orderId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

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

export async function cancelSellerOrder(orderId, reason) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/orders/${orderId}/cancel`,
      reason ? { reason } : {},
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
