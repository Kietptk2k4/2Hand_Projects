import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { authorAvatarUrl, authorDisplayName } from "./authorDisplay";

export function mapApiCommentToListItem(data, authorOverride) {
  const author = authorOverride || {
    userId: data.authorId,
    displayName: authorDisplayName(data.authorId),
    avatarUrl: authorAvatarUrl(data.authorId),
  };

  return {
    commentId: data.commentId,
    postId: data.postId,
    parentCommentId: data.parentCommentId ?? null,
    author,
    contentText: data.contentText,
    media: data.media || [],
    likeCount: 0,
    replyCount: 0,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  };
}

export function buildAuthorFromSessionUser(user) {
  if (!user?.id) {
    return {
      userId: "",
      displayName: DEFAULT_USER_DISPLAY_NAME,
      avatarUrl: authorAvatarUrl(""),
    };
  }
  return {
    userId: user.id,
    displayName: user.display_name || user.email || authorDisplayName(user.id),
    avatarUrl: user.avatar_url || user.profile?.avatar_url || authorAvatarUrl(user.id),
  };
}
