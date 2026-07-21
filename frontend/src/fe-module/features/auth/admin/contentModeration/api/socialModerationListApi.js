import { socialApiClient } from "../../../../../services/http/socialApiClient";

function normalizeErrors(errors) {
  if (!errors) return [];
  if (Array.isArray(errors)) return errors;
  if (typeof errors === "object") {
    return Object.entries(errors).map(([field, reason]) => ({
      field,
      reason: Array.isArray(reason) ? reason[0] : reason,
    }));
  }
  return [];
}

function unwrapResponse(response) {
  const payload = response?.data;
  if (!payload || payload.success !== true) {
    throw {
      code: payload?.code || response?.status || 500,
      message: payload?.message || "Co loi xay ra. Vui long thu lai.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

function mapAxiosError(error) {
  const status = error?.response?.status || 500;
  const payload = error?.response?.data;
  return {
    code: payload?.code || status,
    message: payload?.message || "Co loi xay ra. Vui long thu lai.",
    errors: normalizeErrors(payload?.errors),
  };
}

async function request(method, url, { params } = {}) {
  try {
    const response = await socialApiClient.request({ method, url, params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getPostsForModeration({
  status,
  moderation_status: moderationStatus,
  q,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  const params = { sort, page, size };
  if (status) params.status = status;
  if (moderationStatus) params.moderation_status = moderationStatus;
  if (q) params.q = q;
  return request("get", "/api/v1/social/admin/posts", { params });
}

export async function getPostForModeration(postId) {
  if (!postId) {
    throw {
      code: 400,
      message: "postId khong hop le.",
      errors: [],
    };
  }
  return request("get", `/api/v1/social/admin/posts/${postId}`);
}

export async function getCommentsForModeration({
  status,
  post_id: postId,
  q,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  const params = { sort, page, size };
  if (status) params.status = status;
  if (postId) params.post_id = postId;
  if (q) params.q = q;
  return request("get", "/api/v1/social/admin/comments", { params });
}
