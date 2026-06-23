import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { withClientUploadOrigin } from "../../../shared/utils/getClientUploadOrigin";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

function mapShopMediaUploadUrlResponse(data) {
  return {
    uploadUrl: data.upload_url,
    objectKey: data.object_key,
    mediaUrl: data.media_url,
    mediaKind: data.media_kind,
    expiresAt: data.expires_at,
    maxFileSizeBytes: data.max_file_size_bytes,
    allowedContentTypes: data.allowed_content_types,
  };
}

export async function requestShopMediaUploadUrl({ contentType, fileSizeBytes, mediaKind }) {
  try {
    const response = await commerceApiClient.post(
      "/commerce/api/v1/seller/shop/media/upload-url",
      withClientUploadOrigin({
        content_type: contentType,
        file_size_bytes: fileSizeBytes,
        media_kind: mediaKind,
      }),
    );
    return mapShopMediaUploadUrlResponse(unwrapResponse(response));
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function uploadShopMediaFile(uploadUrl, file) {
  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": file.type },
    body: file,
  });

  if (!response.ok) {
    throw {
      code: response.status,
      message: "Upload ảnh thất bại. Vui lòng thử lại.",
    };
  }
}

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
