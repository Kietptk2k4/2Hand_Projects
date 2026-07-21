function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapPostModerationListItem(item) {
  if (!item) return null;
  return {
    id: item.id,
    author_id: pick(item, "authorId", "author_id"),
    author_display_name: pick(item, "authorDisplayName", "author_display_name") || "",
    author_avatar_url: pick(item, "authorAvatarUrl", "author_avatar_url") || "",
    caption_preview: pick(item, "captionPreview", "caption_preview") || "",
    thumbnail_url: pick(item, "thumbnailUrl", "thumbnail_url") || "",
    media_count: item.media_count ?? item.mediaCount ?? 0,
    status: item.status,
    moderation_status: pick(item, "moderationStatus", "moderation_status"),
    like_count: item.like_count ?? item.likeCount ?? 0,
    created_at: pick(item, "createdAt", "created_at"),
    updated_at: pick(item, "updatedAt", "updated_at"),
  };
}

export function mapPostModerationListResponse(data) {
  if (!data) {
    return { items: [], pagination: null };
  }

  return {
    items: (data.items || []).map(mapPostModerationListItem).filter(Boolean),
    pagination: data.pagination || null,
  };
}

export function resolvePostAuthorSummary(item, authorSummaries = {}) {
  if (!item?.author_id) return null;

  const fromApi = {
    userId: item.author_id,
    displayName: item.author_display_name || "",
    avatarUrl: item.author_avatar_url || "",
  };

  if (fromApi.displayName || fromApi.avatarUrl) {
    return fromApi;
  }

  const fallback = authorSummaries[item.author_id];
  if (!fallback) return fromApi;

  return {
    userId: item.author_id,
    displayName: fallback.displayName || fallback.display_name || "",
    avatarUrl: fallback.avatarUrl || fallback.avatar_url || "",
  };
}
