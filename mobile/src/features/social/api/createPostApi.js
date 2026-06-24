import {
  logPostMediaPresignFail,
  logPostMediaPresignOk,
  logPostMediaPresignRequest,
  logPostMediaPutFail,
  logPostMediaPutOk,
  logPostMediaPutStart,
} from "../../../shared/utils/debugMediaLog";
import { getClientUploadOrigin } from "../../../shared/utils/getDevMediaHost";
import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";
import * as FileSystem from "expo-file-system/legacy";
import { resolveUploadableUri } from "../utils/postMediaFileUtils";

function mapUploadUrlResponse(data) {
  return {
    // Presigned URL is bound to host + signature — never rewrite upload_url on client.
    uploadUrl: data.upload_url,
    objectKey: data.object_key,
    // Persist canonical media_url from server; rewrite only when displaying (normalizePostMediaUrl).
    mediaUrl: data.media_url,
    mediaKind: data.media_kind,
    expiresAt: data.expires_at,
    maxFileSizeBytes: data.max_file_size_bytes,
    allowedContentTypes: data.allowed_content_types,
  };
}

export async function requestPostMediaUploadUrl({ contentType, fileSizeBytes, mediaKind }) {
  logPostMediaPresignRequest({ contentType, fileSizeBytes, mediaKind });

  const clientUploadOrigin = getClientUploadOrigin();
  const payload = {
    content_type: contentType,
    file_size_bytes: fileSizeBytes,
    media_kind: mediaKind,
    ...(clientUploadOrigin ? { client_upload_origin: clientUploadOrigin } : {}),
  };

  try {
    const response = await socialApiClient.post("/api/v1/social/posts/media/upload-url", payload);
    const meta = mapUploadUrlResponse(unwrapResponse(response));
    logPostMediaPresignOk(meta);
    return meta;
  } catch (error) {
    const mapped = mapAxiosError(error);
    logPostMediaPresignFail(mapped);
    throw mapped;
  }
}

export async function uploadPostMediaFile(uploadUrl, { uri, mimeType }) {
  let localUri;
  let fileSize;
  try {
    const resolved = await resolveUploadableUri(uri, mimeType);
    localUri = resolved.uri;
    fileSize = resolved.size;
  } catch (error) {
    logPostMediaPutFail({
      uploadUrl,
      error: error?.message ? error : { message: String(error) },
    });
    throw error?.message
      ? error
      : { code: "LOCAL_FILE_READ", message: "Không đọc được file từ thiết bị." };
  }

  logPostMediaPutStart({ uploadUrl, blobSize: fileSize, mimeType });

  let result;
  try {
    result = await FileSystem.uploadAsync(uploadUrl, localUri, {
      httpMethod: "PUT",
      uploadType: FileSystem.FileSystemUploadType.BINARY_CONTENT,
      headers: { "Content-Type": mimeType },
    });
  } catch (error) {
    logPostMediaPutFail({
      uploadUrl,
      error: { message: error?.message || String(error) },
    });
    throw {
      code: "MINIO_PUT_NETWORK",
      message: "Không kết nối được MinIO upload URL. Kiểm tra SOCIAL_MINIO_PUBLIC_URL / LAN.",
    };
  }

  if (result.status < 200 || result.status >= 300) {
    const error = {
      code: result.status,
      message: "Upload media thất bại. Vui lòng thử lại.",
    };
    logPostMediaPutFail({
      uploadUrl,
      status: result.status,
      error,
      responseBody: result.body?.slice?.(0, 200) ?? null,
    });
    throw error;
  }

  logPostMediaPutOk({ uploadUrl, status: result.status });
}

export async function createPost(payload) {
  if (__DEV__) {
    console.log("[post-create] request", {
      publish: payload?.publish,
      visibility: payload?.visibility,
      mediaCount: payload?.media?.length ?? 0,
      mediaUrls: (payload?.media || []).map((item) => item?.url).filter(Boolean),
    });
  }

  try {
    const response = await socialApiClient.post("/api/v1/social/posts", payload);
    return unwrapResponse(response);
  } catch (error) {
    const mapped = mapAxiosError(error);
    if (__DEV__) {
      console.warn("[post-create] fail", {
        code: mapped?.code ?? null,
        message: mapped?.message ?? String(error),
        errors: mapped?.errors ?? null,
      });
    }
    throw mapped;
  }
}
