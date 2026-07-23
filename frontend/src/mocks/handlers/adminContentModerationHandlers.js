import { delay, http, HttpResponse } from "msw";
import {
  getProductModerationHistory,
  removeProductByAdmin,
  restoreProductByAdmin,
} from "../data/commerceAdminProductRemovalData";
import { moderateAdminReview } from "../data/commerceAdminReviewModerationData";
import { moderateAdminShop, getShopModerationHistory } from "../data/commerceAdminShopModerationData";
import {
  moderateCommentByAdmin,
  moderatePostByAdmin,
  getCommentModerationHistory,
  getPostModerationHistory,
  restoreCommentByAdmin,
  restorePostByAdmin,
} from "../data/adminSocialModerationData";
import {
  getModerationCommentDetail,
  getModerationPostDetail,
  listModerationComments,
  listModerationPosts,
} from "../data/adminSocialModerationListData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function nextModerationLogId(prefix) {
  return `${prefix}-${Date.now().toString(36)}`;
}

function requireAdmin(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("ADMIN-401", "Authentication required"), { status: 401 }),
    };
  }
  if (!user.is_admin) {
    return {
      error: HttpResponse.json(apiError("ADMIN-403", "Missing admin permission"), { status: 403 }),
    };
  }
  return { user };
}

function mapResult(result) {
  if (result.error) {
    return HttpResponse.json(
      apiError(result.error, result.message || "Co loi xay ra."),
      { status: result.status || 400 },
    );
  }
  return HttpResponse.json(apiSuccess(200, result.message, result.data), { status: 200 });
}

async function readJsonBody(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

function mapShopAdminResponse(result, endpoint) {
  const actedAt = result.data?.moderated_at || new Date().toISOString();
  const base = {
    shop_id: result.data?.shop_id,
    moderation_log_id: nextModerationLogId("smh"),
    reason: result.data?.reason,
    outbox_event_id: nextModerationLogId("ob"),
  };

  if (endpoint === "suspend") {
    return { ...base, suspended_at: actedAt };
  }
  if (endpoint === "close") {
    return { ...base, closed_at: actedAt };
  }
  return { ...base, reopened_at: actedAt };
}

function mapReviewAdminResponse(result, endpoint) {
  const actedAt = result.data?.moderated_at || new Date().toISOString();
  const base = {
    review_id: result.data?.review_id,
    moderation_log_id: nextModerationLogId("rmh"),
    reason: result.data?.reason,
    outbox_event_id: nextModerationLogId("ob"),
  };

  if (endpoint === "hide") {
    return { ...base, hidden_at: actedAt };
  }
  if (endpoint === "remove") {
    return { ...base, removed_at: actedAt };
  }
  return { ...base, restored_at: actedAt };
}

const SHOP_ENDPOINT_ACTION = {
  suspend: "SUSPEND",
  close: "CLOSE",
  reopen: "RESTORE",
};

const REVIEW_ENDPOINT_ACTION = {
  hide: "HIDE",
  remove: "REMOVE",
  restore: "RESTORE",
};

export const adminContentModerationHandlers = [
  http.get("*/api/v1/social/admin/posts", async ({ request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const data = listModerationPosts({
      status: url.searchParams.get("status") || "",
      moderation_status: url.searchParams.get("moderation_status") || "",
      q: (url.searchParams.get("q") || "").trim(),
      sort: url.searchParams.get("sort") || "created_at",
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach bai viet kiem duyet thanh cong.", data),
      { status: 200 },
    );
  }),

  http.get("*/api/v1/social/admin/posts/:postId", async ({ request, params }) => {
    await delay(250);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const detail = getModerationPostDetail(params.postId);
    if (detail.error) {
      return HttpResponse.json(apiError(detail.error, detail.message), { status: detail.status || 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet bai viet kiem duyet thanh cong.", detail),
      { status: 200 },
    );
  }),

  http.get("*/api/v1/social/admin/comments", async ({ request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const data = listModerationComments({
      status: url.searchParams.get("status") || "",
      moderation_status: url.searchParams.get("moderation_status") || "",
      post_id: (url.searchParams.get("post_id") || "").trim(),
      q: (url.searchParams.get("q") || "").trim(),
      sort: url.searchParams.get("sort") || "created_at",
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach binh luan kiem duyet thanh cong.", data),
      { status: 200 },
    );
  }),

  http.get("*/api/v1/social/admin/comments/:commentId", async ({ request, params }) => {
    await delay(250);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const detail = getModerationCommentDetail(params.commentId);
    if (detail.error) {
      return HttpResponse.json(apiError(detail.error, detail.message), { status: detail.status || 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet binh luan kiem duyet thanh cong.", detail),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/shops/:shopId/suspend", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminShop(
      params.shopId,
      { action: SHOP_ENDPOINT_ACTION.suspend, reason: body.reason, note: body.note },
      { isAdmin: true, permissions: [] },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapShopAdminResponse(result, "suspend")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/shops/:shopId/close", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminShop(
      params.shopId,
      { action: SHOP_ENDPOINT_ACTION.close, reason: body.reason, note: body.note },
      { isAdmin: true, permissions: [] },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapShopAdminResponse(result, "close")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/shops/:shopId/reopen", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminShop(
      params.shopId,
      { action: SHOP_ENDPOINT_ACTION.reopen, reason: body.reason, note: body.note },
      { isAdmin: true, permissions: [] },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapShopAdminResponse(result, "reopen")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/products/:productId/remove", async ({ params, request }) => {
    await delay(400);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = removeProductByAdmin(params.productId, body, { isAdmin: true });
    return mapResult(result);
  }),

  http.post("*/admin/api/v1/products/:productId/restore", async ({ params, request }) => {
    await delay(400);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = restoreProductByAdmin(params.productId, body, { isAdmin: true });
    return mapResult(result);
  }),

  http.get("*/admin/api/v1/products/:productId/moderation-history", async ({ params, request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = getProductModerationHistory(params.productId, {
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });
    return mapResult(result);
  }),

  http.post("*/admin/api/v1/reviews/:reviewId/hide", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminReview(
      params.reviewId,
      { action: REVIEW_ENDPOINT_ACTION.hide, reason: body.reason, note: body.note },
      { isAdmin: true },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapReviewAdminResponse(result, "hide")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/reviews/:reviewId/remove", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminReview(
      params.reviewId,
      { action: REVIEW_ENDPOINT_ACTION.remove, reason: body.reason, note: body.note },
      { isAdmin: true },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapReviewAdminResponse(result, "remove")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/reviews/:reviewId/restore", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = moderateAdminReview(
      params.reviewId,
      { action: REVIEW_ENDPOINT_ACTION.restore, reason: body.reason, note: body.note },
      { isAdmin: true },
    );
    if (result.error) return mapResult(result);

    return HttpResponse.json(
      apiSuccess(200, result.message, mapReviewAdminResponse(result, "restore")),
      { status: 200 },
    );
  }),

  http.post("*/admin/api/v1/social/posts/:postId/moderate", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), { status: 400 });
    }

    const result = moderatePostByAdmin(params.postId, body);
    return mapResult(result);
  }),

  http.post("*/admin/api/v1/social/posts/:postId/restore", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), { status: 400 });
    }

    const result = restorePostByAdmin(params.postId, body);
    return mapResult(result);
  }),

  http.get("*/admin/api/v1/social/posts/:postId/moderation-history", async ({ params, request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = getPostModerationHistory(params.postId, {
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });
    return mapResult(result);
  }),

  http.post("*/admin/api/v1/social/comments/:commentId/moderate", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), { status: 400 });
    }

    const result = moderateCommentByAdmin(params.commentId, body);
    return mapResult(result);
  }),

  http.post("*/admin/api/v1/social/comments/:commentId/restore", async ({ params, request }) => {
    await delay(350);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const body = await readJsonBody(request);
    if (!body) {
      return HttpResponse.json(apiError("ADMIN-400-VALIDATION", "Du lieu khong hop le."), { status: 400 });
    }

    const result = restoreCommentByAdmin(params.commentId, body);
    return mapResult(result);
  }),

  http.get("*/admin/api/v1/social/comments/:commentId/moderation-history", async ({ params, request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = getCommentModerationHistory(params.commentId, {
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });
    return mapResult(result);
  }),

  http.get("*/admin/api/v1/shops/:shopId/moderation-history", async ({ params, request }) => {
    await delay(300);
    const auth = requireAdmin(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const result = getShopModerationHistory(params.shopId, {
      page: url.searchParams.get("page") || 1,
      size: url.searchParams.get("size") || 20,
    });
    return mapResult(result);
  }),
];
