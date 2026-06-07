import { authorAvatarUrl, authorDisplayName } from "./authorDisplay";

function resolveAuthorFromApi(data) {
  const authorFromApi = data?.author ?? data?.Author;
  if (!authorFromApi) {
    return {
      userId: data.authorId,
      displayName: authorDisplayName(data.authorId),
      avatarUrl: authorAvatarUrl(data.authorId),
    };
  }

  return {
    userId: authorFromApi.userId ?? authorFromApi.user_id ?? data.authorId,
    displayName: authorFromApi.displayName ?? authorFromApi.display_name,
    avatarUrl: authorFromApi.avatarUrl ?? authorFromApi.avatar_url ?? null,
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
    likeCount: 0,
    replyCount: 0,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  };
}