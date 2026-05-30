import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchPostDetail(postId) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/posts/${postId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deletePost(postId) {
  try {
    const response = await socialApiClient.delete(`/api/v1/social/posts/${postId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchPostComments(postId, { page = 0, size = 20, parentCommentId, sort } = {}) {
  try {
    const params = { page, size };
    if (parentCommentId) params.parent_comment_id = parentCommentId;
    if (sort) params.sort = sort;

    const response = await socialApiClient.get(`/api/v1/social/posts/${postId}/comments`, {
      params,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
