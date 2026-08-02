import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

function mapPostItem(item) {
  if (!item) return item;
  return {
    ...item,
    postId: item.postId || item.post_id || item.id,
    authorId: item.authorId || item.author_id || item.author?.userId || item.author?.id,
    likeCount: item.likeCount ?? item.like_count ?? 0,
    replyCount: item.replyCount ?? item.reply_count ?? 0,
    createdAt: item.createdAt || item.created_at,
  };
}

export async function fetchUserPosts(userId, { page = 0, size = 12, statusFilter = "published" } = {}) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/posts`, {
      params: {
        page,
        size,
        status_filter: statusFilter,
      },
    });
    const data = unwrapResponse(response);
    if (data?.items) {
      return {
        ...data,
        items: data.items.map(mapPostItem),
      };
    }
    return data;
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchUserRepliedPosts(userId, { page = 0, size = 12 } = {}) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/replied-posts`, {
      params: {
        page,
        size,
      },
    });
    const data = unwrapResponse(response);
    if (data?.items) {
      return {
        ...data,
        items: data.items.map(mapPostItem),
      };
    }
    return data;
  } catch (error) {
    throw mapAxiosError(error);
  }
}
