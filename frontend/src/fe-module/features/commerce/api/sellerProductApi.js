import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { withClientUploadOrigin } from "../../../shared/utils/getClientUploadOrigin";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchSellerProductList({ page, limit, status, q }) {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    if (q) params.q = q;

    const response = await commerceApiClient.get("/commerce/api/v1/seller/products", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSellerProductDetail(productId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/seller/products/${productId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateProduct(productId, payload) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/seller/products/${productId}`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateProductAttributes(productId, payload) {
  try {
    const response = await commerceApiClient.put(
      `/commerce/api/v1/seller/products/${productId}/attributes`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

function mapProductMediaUploadUrlResponse(data) {
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

export async function requestProductMediaUploadUrl(productId, { contentType, fileSizeBytes, mediaKind }) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/media/upload-url`,
      withClientUploadOrigin({
        content_type: contentType,
        file_size_bytes: fileSizeBytes,
        media_kind: mediaKind,
      }),
    );
    return mapProductMediaUploadUrlResponse(unwrapResponse(response));
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function uploadProductMediaFile(uploadUrl, file) {
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

export async function updateProductMedia(productId, payload) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/seller/products/${productId}/media`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createProduct(payload) {
  try {
    const response = await commerceApiClient.post("/commerce/api/v1/seller/products", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateProductPrice(productId, payload) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/prices`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateProductInventory(productId, payload) {
  try {
    const response = await commerceApiClient.patch(
      `/commerce/api/v1/seller/products/${productId}/inventory`,
      payload,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function publishProduct(productId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/publish`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function pauseProduct(productId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/pause`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function archiveProduct(productId) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/archive`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
