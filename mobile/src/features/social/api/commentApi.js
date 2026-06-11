import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, normalizeErrors, unwrapResponse } from "./socialApiResponse";

function unwrapCreatedResponse(response) {
  const payload = response?.data;
  if (!payload || payload.success !== true) {
    throw {
      code: payload?.code || response?.status || 500,
      message: payload?.message || "Co loi xay ra. Vui long thu lai.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

export async function createPostComment(postId, { contentText, media = [] }) {
  try {
    const response = await socialApiClient.post(
      `/api/v1/social/posts/${postId}/comments`,
      { contentText, media }
    );
    return unwrapCreatedResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createCommentReply(commentId, { contentText, media = [] }) {
  try {
    const response = await socialApiClient.post(
      `/api/v1/social/comments/${commentId}/replies`,
      { contentText, media }
    );
    return unwrapCreatedResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
