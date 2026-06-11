import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchHashtagPosts(hashtag, { page = 0, size = 20 } = {}) {
  const normalized = hashtag?.replace(/^#+/, "").trim();
  try {
    const response = await socialApiClient.get(
      `/api/v1/social/search/hashtags/${encodeURIComponent(normalized)}`,
      { params: { page, size } }
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
