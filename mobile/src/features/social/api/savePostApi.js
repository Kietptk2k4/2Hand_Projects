import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function toggleSavePost(postId) {
  try {
    const response = await socialApiClient.post(`/api/v1/social/posts/${postId}/save`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
