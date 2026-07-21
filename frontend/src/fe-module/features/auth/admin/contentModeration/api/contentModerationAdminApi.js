import { adminApiClient } from "../../../../../services/http/adminApiClient";

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

async function request(method, url, { params, data } = {}) {
  try {
    const response = await adminApiClient.request({ method, url, params, data });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

function moderationBody({ reason, note }) {
  const body = { reason: String(reason ?? "").trim() };
  const trimmedNote = String(note ?? "").trim();
  if (trimmedNote) body.note = trimmedNote;
  return body;
}

export async function suspendShop(shopId, payload) {
  return request("post", `/admin/api/v1/shops/${shopId}/suspend`, {
    data: moderationBody(payload),
  });
}

export async function closeShop(shopId, payload) {
  return request("post", `/admin/api/v1/shops/${shopId}/close`, {
    data: moderationBody(payload),
  });
}

export async function reopenShop(shopId, payload) {
  return request("post", `/admin/api/v1/shops/${shopId}/reopen`, {
    data: moderationBody(payload),
  });
}

export async function removeProduct(productId, payload) {
  return request("post", `/admin/api/v1/products/${productId}/remove`, {
    data: moderationBody(payload),
  });
}

export async function restoreProduct(productId, payload) {
  return request("post", `/admin/api/v1/products/${productId}/restore`, {
    data: moderationBody(payload),
  });
}

export async function fetchProductModerationHistory(productId, { page = 1, size = 20 } = {}) {
  return request("get", `/admin/api/v1/products/${productId}/moderation-history`, {
    params: { page, size },
  });
}

export async function fetchPostModerationHistory(postId, { page = 1, size = 20 } = {}) {
  return request("get", `/admin/api/v1/social/posts/${postId}/moderation-history`, {
    params: { page, size },
  });
}

export async function hideReview(reviewId, payload) {
  return request("post", `/admin/api/v1/reviews/${reviewId}/hide`, {
    data: moderationBody(payload),
  });
}

export async function removeReview(reviewId, payload) {
  return request("post", `/admin/api/v1/reviews/${reviewId}/remove`, {
    data: moderationBody(payload),
  });
}

export async function restoreReview(reviewId, payload) {
  return request("post", `/admin/api/v1/reviews/${reviewId}/restore`, {
    data: moderationBody(payload),
  });
}

export async function moderatePost(postId, payload) {
  return request("post", `/admin/api/v1/social/posts/${postId}/moderate`, {
    data: {
      action: payload.action,
      ...moderationBody(payload),
    },
  });
}

export async function restorePost(postId, payload) {
  return request("post", `/admin/api/v1/social/posts/${postId}/restore`, {
    data: moderationBody(payload),
  });
}

export async function moderateComment(commentId, payload) {
  return request("post", `/admin/api/v1/social/comments/${commentId}/moderate`, {
    data: {
      action: payload.action,
      ...moderationBody(payload),
    },
  });
}

export async function restoreComment(commentId, payload) {
  return request("post", `/admin/api/v1/social/comments/${commentId}/restore`, {
    data: moderationBody(payload),
  });
}