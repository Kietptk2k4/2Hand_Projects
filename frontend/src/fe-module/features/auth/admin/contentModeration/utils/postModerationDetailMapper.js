function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapMediaItem(item) {
  if (!item) return null;
  return {
    url: item.url,
    type: item.type,
    width: item.width ?? null,
    height: item.height ?? null,
  };
}

export function mapPostModerationDetailResponse(data) {
  if (!data) return null;

  const author = data.author || {};
  return {
    id: data.id,
    author: {
      userId: pick(author, "userId", "user_id"),
      displayName: pick(author, "displayName", "display_name") || "",
      avatarUrl: pick(author, "avatarUrl", "avatar_url") || "",
    },
    caption: data.caption || "",
    media: (data.media || []).map(mapMediaItem).filter(Boolean),
    thumbnail_url: pick(data, "thumbnailUrl", "thumbnail_url") || "",
    media_count: data.media_count ?? data.mediaCount ?? 0,
    status: data.status,
    moderation_status: pick(data, "moderationStatus", "moderation_status"),
    moderation_reason: pick(data, "moderationReason", "moderation_reason") || "",
    last_moderation_log_id: pick(data, "lastModerationLogId", "last_moderation_log_id") || "",
    visibility: data.visibility,
    like_count: data.like_count ?? data.likeCount ?? 0,
    reply_count: data.reply_count ?? data.replyCount ?? 0,
    hashtags: data.hashtags || [],
    allow_comments: data.allow_comments ?? data.allowComments ?? true,
    created_at: pick(data, "createdAt", "created_at"),
    updated_at: pick(data, "updatedAt", "updated_at"),
  };
}
