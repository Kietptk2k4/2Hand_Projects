import { ADMIN_CONTENT_MODERATION_QA } from "../../fe-module/shared/constants/adminContentModerationQaIds.js";
import { isValidObjectId } from "../../fe-module/features/auth/admin/contentModeration/utils/isValidObjectId.js";

const NOT_FOUND_ID = ADMIN_CONTENT_MODERATION_QA.post.notFound;
const MOCK_ADMIN_ID = "a1000000-0000-4000-8000-000000000001";

/** @type {Map<string, Array<object>>} */
const moderationHistoryByPostId = new Map();

function nextId(prefix) {
  return `${prefix}-${Date.now().toString(36)}`;
}

function appendPostHistory(postId, entry) {
  const list = moderationHistoryByPostId.get(postId) || [];
  moderationHistoryByPostId.set(postId, [entry, ...list]);
}

function seedPostHistory() {
  appendPostHistory(ADMIN_CONTENT_MODERATION_QA.post.sample, {
    moderation_log_id: "pmh-0000-4000-8000-000000000001",
    action: "HIDE",
    reason: "Noi dung co dau hieu spam.",
    note: null,
    admin_id: MOCK_ADMIN_ID,
    created_at: "2026-05-18T14:30:00Z",
  });
}

seedPostHistory();

function validateReason(reason) {
  const trimmed = String(reason ?? "").trim();
  if (!trimmed) {
    return { error: "ADMIN-400-VALIDATION", status: 400, message: "Ly do la bat buoc." };
  }
  return { reason: trimmed };
}

function validateObjectId(id) {
  if (!isValidObjectId(id)) {
    return { error: "ADMIN-400-VALIDATION", status: 400, message: "ObjectId khong hop le." };
  }
  return null;
}

function validateAction(action) {
  const normalized = String(action ?? "").trim().toUpperCase();
  if (!["HIDE", "REMOVE"].includes(normalized)) {
    return { error: "ADMIN-400-VALIDATION", status: 400, message: "Action phai la HIDE hoac REMOVE." };
  }
  return { action: normalized };
}

function buildPostModerateResponse({ targetId, action, reason, note }) {
  const actedAt = new Date().toISOString();
  const moderationLogId = nextId("cml");

  appendPostHistory(targetId, {
    moderation_log_id: moderationLogId,
    action,
    reason,
    note: note || null,
    admin_id: MOCK_ADMIN_ID,
    created_at: actedAt,
  });

  return {
    data: {
      post_id: targetId,
      action,
      moderation_log_id: moderationLogId,
      reason,
      note: note || null,
      moderated_by: MOCK_ADMIN_ID,
      moderated_at: actedAt,
      outbox_event_id: nextId("ob"),
    },
    message: action === "HIDE" ? "An thanh cong." : "Go thanh cong.",
  };
}

function buildPostRestoreResponse({ targetId, reason, note }) {
  const actedAt = new Date().toISOString();
  const moderationLogId = nextId("cml");

  appendPostHistory(targetId, {
    moderation_log_id: moderationLogId,
    action: "RESTORE",
    reason,
    note: note || null,
    admin_id: MOCK_ADMIN_ID,
    created_at: actedAt,
  });

  return {
    data: {
      post_id: targetId,
      moderation_log_id: moderationLogId,
      reason,
      note: note || null,
      restored_by: MOCK_ADMIN_ID,
      restored_at: actedAt,
      outbox_event_id: nextId("ob"),
    },
    message: "Khoi phuc thanh cong.",
  };
}

function buildCommentModerateResponse({ targetId, action, reason, note }) {
  const actedAt = new Date().toISOString();
  return {
    data: {
      comment_id: targetId,
      action,
      moderation_log_id: nextId("cml"),
      reason,
      note: note || null,
      moderated_by: MOCK_ADMIN_ID,
      moderated_at: actedAt,
      outbox_event_id: nextId("ob"),
    },
    message: action === "HIDE" ? "An thanh cong." : "Go thanh cong.",
  };
}

function buildCommentRestoreResponse({ targetId, reason, note }) {
  const actedAt = new Date().toISOString();
  return {
    data: {
      comment_id: targetId,
      moderation_log_id: nextId("cml"),
      reason,
      note: note || null,
      restored_by: MOCK_ADMIN_ID,
      restored_at: actedAt,
      outbox_event_id: nextId("ob"),
    },
    message: "Khoi phuc thanh cong.",
  };
}

export function moderatePostByAdmin(postId, body) {
  const idError = validateObjectId(postId);
  if (idError) return idError;
  if (postId === NOT_FOUND_ID) {
    return { error: "ADMIN-404", status: 404, message: "Post not found." };
  }

  const actionResult = validateAction(body?.action);
  if (actionResult.error) return actionResult;
  const reasonResult = validateReason(body?.reason);
  if (reasonResult.error) return reasonResult;

  return buildPostModerateResponse({
    targetId: postId,
    action: actionResult.action,
    reason: reasonResult.reason,
    note: body?.note,
  });
}

export function restorePostByAdmin(postId, body) {
  const idError = validateObjectId(postId);
  if (idError) return idError;
  if (postId === NOT_FOUND_ID) {
    return { error: "ADMIN-404", status: 404, message: "Post not found." };
  }

  const reasonResult = validateReason(body?.reason);
  if (reasonResult.error) return reasonResult;

  return buildPostRestoreResponse({
    targetId: postId,
    reason: reasonResult.reason,
    note: body?.note,
  });
}

export function getPostModerationHistory(postId, { page = 1, size = 20 } = {}) {
  const idError = validateObjectId(postId);
  if (idError) return idError;

  const pageNum = Number(page);
  const sizeNum = Number(size);

  if (!Number.isInteger(pageNum) || pageNum < 1 || !Number.isInteger(sizeNum) || sizeNum < 1) {
    return { error: "ADMIN-400-PAGINATION", status: 400 };
  }

  const all = moderationHistoryByPostId.get(postId) || [];
  const totalElements = all.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / sizeNum) || 1);
  const start = (pageNum - 1) * sizeNum;

  return {
    data: {
      post_id: postId,
      page: pageNum,
      size: sizeNum,
      total_elements: totalElements,
      total_pages: totalPages,
      history: all.slice(start, start + sizeNum),
    },
    message: "Post moderation history retrieved successfully",
  };
}

export function moderateCommentByAdmin(commentId, body) {
  const idError = validateObjectId(commentId);
  if (idError) return idError;
  if (commentId === NOT_FOUND_ID) {
    return { error: "ADMIN-404", status: 404, message: "Comment not found." };
  }

  const actionResult = validateAction(body?.action);
  if (actionResult.error) return actionResult;
  const reasonResult = validateReason(body?.reason);
  if (reasonResult.error) return reasonResult;

  return buildCommentModerateResponse({
    targetId: commentId,
    action: actionResult.action,
    reason: reasonResult.reason,
    note: body?.note,
  });
}

export function restoreCommentByAdmin(commentId, body) {
  const idError = validateObjectId(commentId);
  if (idError) return idError;
  if (commentId === NOT_FOUND_ID) {
    return { error: "ADMIN-404", status: 404, message: "Comment not found." };
  }

  const reasonResult = validateReason(body?.reason);
  if (reasonResult.error) return reasonResult;

  return buildCommentRestoreResponse({
    targetId: commentId,
    reason: reasonResult.reason,
    note: body?.note,
  });
}
