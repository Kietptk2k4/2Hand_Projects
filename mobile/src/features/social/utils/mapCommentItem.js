import { authorAvatarUrl, authorDisplayName } from "./authorDisplay";

import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";

function resolveAuthorFromApi(data) {
  const authorFromApi = data?.author;
  if (!authorFromApi) {
    return {
      userId: data.authorId,
      displayName: authorDisplayName(data.authorId),
      avatarUrl: authorAvatarUrl(data.authorId),
    };
  }

  return {
    userId: authorFromApi.userId ?? data.authorId,
    displayName: authorFromApi.displayName,
    avatarUrl: resolveDevMediaUrl(authorFromApi.avatarUrl ?? null),
  };
}

export function mapApiCommentToListItem(data) {
  return {
    commentId: data.commentId,
    postId: data.postId,
    parentCommentId: data.parentCommentId ?? null,
    author: resolveAuthorFromApi(data),
    contentText: data.contentText,
    media: data.media || [],
    likeCount: Number(data.likeCount) || 0,
    likedByMe: Boolean(data.likedByMe),
    replyCount: Number(data.replyCount) || 0,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  };
}
