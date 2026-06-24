import { getClientUploadOrigin } from "../../../shared/utils/getDevMediaHost";
import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";
import * as FileSystem from "expo-file-system/legacy";
import { resolveUploadableUri } from "../../social/utils/postMediaFileUtils";

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

function buildPresignPayload({ contentType, fileSizeBytes, mediaKind }) {
  const clientUploadOrigin = getClientUploadOrigin();
  return {
    content_type: contentType,
    file_size_bytes: fileSizeBytes,
    media_kind: mediaKind,
    ...(clientUploadOrigin ? { client_upload_origin: clientUploadOrigin } : {}),
  };
}

export async function requestProductMediaUploadUrl(productId, { contentType, fileSizeBytes, mediaKind }) {
  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/seller/products/${productId}/media/upload-url`,
      buildPresignPayload({ contentType, fileSizeBytes, mediaKind }),
    );
    return mapProductMediaUploadUrlResponse(unwrapResponse(response));
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function uploadProductMediaFile(uploadUrl, { uri, mimeType }) {
  let localUri;
  try {
    const resolved = await resolveUploadableUri(uri, mimeType);
    localUri = resolved.uri;
  } catch (error) {
    throw error?.message
      ? error
      : { code: "LOCAL_FILE_READ", message: "Không đọc được file từ thiết bị." };
  }

  let result;
  try {
    result = await FileSystem.uploadAsync(uploadUrl, localUri, {
      httpMethod: "PUT",
      uploadType: FileSystem.FileSystemUploadType.BINARY_CONTENT,
      headers: { "Content-Type": mimeType },
    });
  } catch (error) {
    throw {
      code: "MINIO_PUT_NETWORK",
      message: "Không kết nối được MinIO upload URL. Kiểm tra COMMERCE_MINIO_PUBLIC_URL / LAN.",
    };
  }

  if (result.status < 200 || result.status >= 300) {
    throw {
      code: result.status,
      message: "Upload ảnh/video sản phẩm thất bại. Vui lòng thử lại.",
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
