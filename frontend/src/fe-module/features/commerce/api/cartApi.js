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

export async function addProductToCart({ productId, quantity }) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/cart/items", {
      product_id: productId,
      quantity,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function validateCartItems(cartItemIds) {
  try {
    const body = cartItemIds?.length ? { cart_item_ids: cartItemIds } : {};
    const response = await commerceApiClient.post("/commerce/api/v1/cart/validate", body);
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
