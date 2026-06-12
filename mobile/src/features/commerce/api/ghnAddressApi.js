import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchGhnProvinces() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/shipping/ghn/provinces");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchGhnDistricts(provinceId) {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/shipping/ghn/districts", {
      params: { province_id: provinceId },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchGhnWards(districtId) {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/shipping/ghn/wards", {
      params: { district_id: districtId },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
