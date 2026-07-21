function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapModerationPayload({ reason, note }) {
  const body = { reason: String(reason ?? "").trim() };
  const trimmedNote = String(note ?? "").trim();
  if (trimmedNote) body.note = trimmedNote;
  return body;
}

export function mapShopModerationResponse(data) {
  if (!data) return null;
  return {
    shopId: pick(data, "shopId", "shop_id"),
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedAt:
      pick(data, "suspendedAt", "suspended_at") ||
      pick(data, "closedAt", "closed_at") ||
      pick(data, "reopenedAt", "reopened_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapProductModerationResponse(data) {
  if (!data) return null;
  return {
    productId: pick(data, "productId", "product_id"),
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedAt:
      pick(data, "removedAt", "removed_at") || pick(data, "restoredAt", "restored_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapReviewModerationResponse(data) {
  if (!data) return null;
  return {
    reviewId: pick(data, "reviewId", "review_id"),
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedAt:
      pick(data, "hiddenAt", "hidden_at") ||
      pick(data, "removedAt", "removed_at") ||
      pick(data, "restoredAt", "restored_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapProductModerationHistoryResponse(data) {
  if (!data) {
    return { productId: null, page: 1, size: 20, totalElements: 0, totalPages: 0, history: [] };
  }
  return {
    productId: pick(data, "productId", "product_id"),
    page: data.page ?? 1,
    size: data.size ?? 20,
    totalElements: data.total_elements ?? data.totalElements ?? 0,
    totalPages: data.total_pages ?? data.totalPages ?? 0,
    history: (data.history || []).map(mapModerationHistoryEntry),
  };
}

export function mapPostModerationHistoryResponse(data) {
  if (!data) {
    return { postId: null, page: 1, size: 20, totalElements: 0, totalPages: 0, history: [] };
  }
  return {
    postId: pick(data, "postId", "post_id"),
    page: data.page ?? 1,
    size: data.size ?? 20,
    totalElements: data.total_elements ?? data.totalElements ?? 0,
    totalPages: data.total_pages ?? data.totalPages ?? 0,
    history: (data.history || []).map(mapModerationHistoryEntry),
  };
}

function mapModerationHistoryEntry(entry) {
  return {
    moderationLogId: pick(entry, "moderationLogId", "moderation_log_id"),
    action: entry.action,
    reason: entry.reason,
    note: entry.note,
    adminId: pick(entry, "adminId", "admin_id"),
    createdAt: pick(entry, "createdAt", "created_at"),
  };
}

export function mapPostModerationResponse(data) {
  if (!data) return null;
  return {
    targetId: pick(data, "postId", "post_id"),
    action: data.action,
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedBy: pick(data, "moderatedBy", "moderated_by"),
    actedAt: pick(data, "moderatedAt", "moderated_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapPostRestoreResponse(data) {
  if (!data) return null;
  return {
    targetId: pick(data, "postId", "post_id"),
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedBy: pick(data, "restoredBy", "restored_by"),
    actedAt: pick(data, "restoredAt", "restored_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapCommentModerationResponse(data) {
  if (!data) return null;
  return {
    targetId: pick(data, "commentId", "comment_id"),
    action: data.action,
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedBy: pick(data, "moderatedBy", "moderated_by"),
    actedAt: pick(data, "moderatedAt", "moderated_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}

export function mapCommentRestoreResponse(data) {
  if (!data) return null;
  return {
    targetId: pick(data, "commentId", "comment_id"),
    moderationLogId: pick(data, "moderationLogId", "moderation_log_id"),
    reason: data.reason,
    note: data.note,
    actedBy: pick(data, "restoredBy", "restored_by"),
    actedAt: pick(data, "restoredAt", "restored_at"),
    outboxEventId: pick(data, "outboxEventId", "outbox_event_id"),
  };
}