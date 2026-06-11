import { feedKeys } from "../api/feedKeys";
import { postKeys } from "../api/postKeys";
import { patchPostDetailCache, patchPostInFeedCaches } from "./postEngagementCache";

export function invalidateFeedAfterCreate(queryClient) {
  return queryClient.invalidateQueries({ queryKey: feedKeys.all });
}

export function applyPostEditToCaches(queryClient, updatedPost) {
  if (!updatedPost?.postId) return;

  const patch = {
    caption: updatedPost.caption,
    media: updatedPost.media,
    hashtags: updatedPost.hashtags,
    visibility: updatedPost.visibility,
    allowComments: updatedPost.allowComments,
    updatedAt: updatedPost.updatedAt,
  };

  patchPostDetailCache(queryClient, updatedPost.postId, patch);
  patchPostInFeedCaches(queryClient, updatedPost.postId, patch);
}
