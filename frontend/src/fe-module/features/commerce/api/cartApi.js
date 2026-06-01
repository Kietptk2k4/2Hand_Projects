import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchCart() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/cart");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateCartItemQuantity(cartItemId, quantity) {
  try {
    const response = await commerceApiClient.patch(`/commerce/api/v1/cart/items/${cartItemId}`, {
      quantity,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function removeCartItem(cartItemId) {
  try {
    const response = await commerceApiClient.delete(`/commerce/api/v1/cart/items/${cartItemId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
