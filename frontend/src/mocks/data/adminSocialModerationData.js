import { ADMIN_CONTENT_MODERATION_QA } from "../../fe-module/shared/constants/adminContentModerationQaIds.js";

const NOT_FOUND_ID = ADMIN_CONTENT_MODERATION_QA.post.notFound;
const MOCK_ADMIN_ID = "a1000000-0000-4000-8000-000000000001";

function nextId(prefix) {
  return `${prefix}-${Date.now().toString(36)}`;
}

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

function buildModerateResponse({ targetKey, targetId, action, reason, note, actedAtKey }) {
  const actedAt = new Date().toISOString();
  return {
    data: {
      [targetKey]: targetId,
      action,
      moderation_log_id: nextId("cml"),
      reason,
      note: note || null,
      moderated_by: MOCK_ADMIN_ID,
      [actedAtKey]: actedAt,
      outbox_event_id: nextId("ob"),
    },
    message: action === "HIDE" ? "An thanh cong." : "Go thanh cong.",
  };
}

function buildRestoreResponse({ targetKey, targetId, reason, note }) {
  const actedAt = new Date().toISOString();
  return {
    data: {
      [targetKey]: targetId,
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

  return buildModerateResponse({
    targetKey: "post_id",
    targetId: postId,
    action: actionResult.action,
    reason: reasonResult.reason,
    note: body?.note,
    actedAtKey: "moderated_at",
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

  return buildRestoreResponse({
    targetKey: "post_id",
    targetId: postId,
    reason: reasonResult.reason,
    note: body?.note,
  });
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

  return buildModerateResponse({
    targetKey: "comment_id",
    targetId: commentId,
    action: actionResult.action,
    reason: reasonResult.reason,
    note: body?.note,
    actedAtKey: "moderated_at",
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

  return buildRestoreResponse({
    targetKey: "comment_id",
    targetId: commentId,
    reason: reasonResult.reason,
    note: body?.note,
  });
}