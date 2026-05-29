import { delay, http, HttpResponse } from "msw";
import { getCommentsForPost } from "../data/socialCommentsData";
import {
  buildPostDetail,
  canViewerSeePost,
  findFeedPost,
  MOCK_POST_ID_NOT_FOUND,
} from "../data/socialPostDetailData";
import { getUserByToken, isValidObjectId } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function parseCommentPagination(url) {
  const pageParam = url.searchParams.get("page");
  const sizeParam = url.searchParams.get("size");
  const page = pageParam === null || pageParam === "" ? 0 : Number(pageParam);
  const size = sizeParam === null || sizeParam === "" ? 20 : Number(sizeParam);

  if (!Number.isInteger(page) || page < 0 || !Number.isInteger(size) || size < 1 || size > 50) {
    return {
      error: {
        code: "SOCIAL-400-PAGINATION",
        success: false,
        message: "Tham so pagination khong hop le.",
        data: null,
        errors: [{ field: "size", reason: "MUST_BE_BETWEEN_1_AND_50" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  return { page, size };
}

function paginateItems(items, page, size) {
  const totalElements = items.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = items.slice(start, start + size);

  return {
    items: slice,
    meta: {
      page,
      size,
      totalElements,
      totalPages,
      hasNext: start + size < totalElements,
    },
  };
}

export const socialPostHandlers = [
  http.get("*/api/v1/social/posts/:postId", async ({ params, request }) => {
    await delay(450);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const postId = params.postId;
    if (!isValidObjectId(postId)) {
      return HttpResponse.json(apiError(400, "postId khong hop le.", null), { status: 400 });
    }

    if (postId === MOCK_POST_ID_NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const feedPost = findFeedPost(postId);
    if (!feedPost) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const access = canViewerSeePost(feedPost, user.id);
    if (!access.allowed) {
      if (access.status === 403) {
        return HttpResponse.json(
          apiError(403, "Ban khong co quyen xem bai viet nay."),
          { status: 403 }
        );
      }
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const detail = buildPostDetail(postId, user.id);
    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet bai viet thanh cong.", detail),
      { status: 200 }
    );
  }),

  http.get("*/api/v1/social/posts/:postId/comments", async ({ params, request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const postId = params.postId;
    const url = new URL(request.url);

    if (!isValidObjectId(postId)) {
      return HttpResponse.json(apiError(400, "postId khong hop le."), { status: 400 });
    }

    const parentCommentId = url.searchParams.get("parent_comment_id");
    if (parentCommentId && !isValidObjectId(parentCommentId)) {
      return HttpResponse.json(apiError(400, "parent_comment_id khong hop le."), { status: 400 });
    }

    const pagination = parseCommentPagination(url);
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    if (postId === MOCK_POST_ID_NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const feedPost = findFeedPost(postId);
    if (!feedPost) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const access = canViewerSeePost(feedPost, user.id);
    if (!access.allowed) {
      if (access.status === 403) {
        return HttpResponse.json(
          apiError(403, "Ban khong co quyen xem binh luan cua bai viet nay."),
          { status: 403 }
        );
      }
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    if (parentCommentId) {
      const allReplies = getCommentsForPost(postId, { parentCommentId });
      if (allReplies.length === 0) {
        return HttpResponse.json(apiError(404, "Khong tim thay binh luan."), { status: 404 });
      }
    }

    const { page, size } = pagination;
    const allComments = getCommentsForPost(postId, { parentCommentId });
    const data = paginateItems(allComments, page, size);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach binh luan thanh cong.", data),
      { status: 200 }
    );
  }),
];
