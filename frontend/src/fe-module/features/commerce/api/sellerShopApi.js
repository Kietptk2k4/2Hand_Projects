import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

/** FE-only GET until backend exposes seller shop detail — MSW implements. */
export async function fetchMyShop() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/seller/shop");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateShopProfile(patch) {
  try {
    const response = await commerceApiClient.patch("/commerce/api/v1/seller/shop", patch);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateShopVacation(body) {
  try {
    const response = await commerceApiClient.patch("/commerce/api/v1/seller/shop/vacation", body);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createShop({ shopName, description, avatarUrl, coverUrl, pickupProfile }) {
  try {
    const body = {
      shop_name: shopName,
    };

    if (description != null && description !== "") {
      body.description = description;
    }
    if (avatarUrl) {
      body.avatar_url = avatarUrl;
    }
    if (coverUrl) {
      body.cover_url = coverUrl;
    }
    if (pickupProfile) {
      body.pickup_profile = {
        pickup_name: pickupProfile.pickupName,
        phone: pickupProfile.phone,
        province_code: pickupProfile.provinceCode,
        district_code: pickupProfile.districtCode,
        ward_code: pickupProfile.wardCode,
        address_detail: pickupProfile.addressDetail,
      };
    }

    const response = await commerceApiClient.post("/commerce/api/v1/seller/shop", body);
    return unwrapResponse(response);
  } catch (error) {
    const mapped = mapAxiosError(error);
    const payload = error?.response?.data;
    if (payload?.data) {
      mapped.data = payload.data;
    }
    throw mapped;
  }
}
