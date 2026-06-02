import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchUserAddresses() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/addresses");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createUserAddress(payload) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/addresses", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateUserAddress(addressId, payload) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/addresses/${addressId}`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deleteUserAddress(addressId) {
  try {
    const response = await commerceApiClient.delete(`/commerce/api/v1/addresses/${addressId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function setDefaultUserAddress(addressId) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/addresses/${addressId}/default`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
