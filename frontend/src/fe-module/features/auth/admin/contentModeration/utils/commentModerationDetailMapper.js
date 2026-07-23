function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapMediaItem(item) {
  if (!item) return null;
  return {
    url: item.url,
    type: item.type,
  };
}

export function mapCommentModerationDetailResponse(data) {
  if (!data) return null;

  const author = data.author || {};
  const parentComment = data.parent_comment || data.parentComment || null;
  const post = data.post || null;

  return {
    id: data.id,
    post_id: pick(data, "postId", "post_id"),
    author: {
      userId: pick(author, "userId", "user_id"),
      displayName: pick(author, "displayName", "display_name") || "",
      avatarUrl: pick(author, "avatarUrl", "avatar_url") || "",
    },
    parent_comment_id: pick(data, "parentCommentId", "parent_comment_id") || "",
    parent_comment: parentComment
      ? {
          id: parentComment.id,
          content_preview: pick(parentComment, "contentPreview", "content_preview") || "",
        }
      : null,
    content_text: pick(data, "contentText", "content_text") || "",
    media: (data.media || []).map(mapMediaItem).filter(Boolean),
    media_count: data.media_count ?? data.mediaCount ?? 0,
    status: data.status,
    moderation_status: pick(data, "moderationStatus", "moderation_status"),
    moderation_reason: pick(data, "moderationReason", "moderation_reason") || "",
    last_moderation_log_id: pick(data, "lastModerationLogId", "last_moderation_log_id") || "",
    like_count: data.like_count ?? data.likeCount ?? 0,
    post: post
      ? {
          id: post.id,
          caption_preview: pick(post, "captionPreview", "caption_preview") || "",
          thumbnail_url: pick(post, "thumbnailUrl", "thumbnail_url") || "",
          moderation_status: pick(post, "moderationStatus", "moderation_status"),
        }
      : null,
    created_at: pick(data, "createdAt", "created_at"),
    updated_at: pick(data, "updatedAt", "updated_at"),
  };
}
