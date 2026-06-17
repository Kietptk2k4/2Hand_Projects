import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchPostLikers(postId, { page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/posts/${postId}/likes`, {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchCommentLikers(commentId, { page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/comments/${commentId}/likes`, {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
