import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchCheckoutQuote({ cartItemIds, addressId, shipmentType }) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/checkout/quote", {
      cart_item_ids: cartItemIds,
      address_id: addressId,
      shipment_type: shipmentType,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchShippingFee({ cartItemIds, addressId, shipmentType }) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/shipping/fee", {
      cart_item_ids: cartItemIds,
      address_id: addressId,
      shipment_type: shipmentType,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function submitCheckout({
  cartItemIds,
  addressId,
  paymentMethod,
  shipmentType,
  idempotencyKey,
}) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/checkout", {
      cart_item_ids: cartItemIds,
      address_id: addressId,
      payment_method: paymentMethod,
      shipment_type: shipmentType,
      idempotency_key: idempotencyKey,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
