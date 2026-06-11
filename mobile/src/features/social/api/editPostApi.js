import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function updatePost(postId, patchBody) {
  try {
    const response = await socialApiClient.put(`/api/v1/social/posts/${postId}`, patchBody);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
