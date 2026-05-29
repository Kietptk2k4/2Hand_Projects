import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

function mapUploadUrlResponse(data) {
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

export async function requestPostMediaUploadUrl({ contentType, fileSizeBytes, mediaKind }) {
  try {
    const response = await socialApiClient.post("/api/v1/social/posts/media/upload-url", {
      content_type: contentType,
      file_size_bytes: fileSizeBytes,
      media_kind: mediaKind,
    });
    return mapUploadUrlResponse(unwrapResponse(response));
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function uploadPostMediaFile(uploadUrl, file) {
  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": file.type },
    body: file,
  });

  if (!response.ok) {
    throw {
      code: response.status,
      message: "Upload media that bai. Vui long thu lai.",
    };
  }
}

export async function createPost(payload) {
  try {
    const response = await socialApiClient.post("/api/v1/social/posts", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
