import { delay, http, HttpResponse } from "msw";
import { buildSavedPostsPage, toggleUserSave } from "../data/socialSavedPostsData";
import { findFeedPost } from "../data/socialPostDetailData";
import { getUserByToken, isValidObjectId } from "../utils/socialMockAuth";
import { checkSocialMockUserCanWrite } from "../utils/socialMockWriteGuard";
import { apiError, apiSuccess } from "../utils/response";

function parsePagination(url) {
  const pageParam = url.searchParams.get("page");
  const sizeParam = url.searchParams.get("size");

  const page = pageParam === null || pageParam === "" ? 0 : Number(pageParam);
  const size = sizeParam === null || sizeParam === "" ? 20 : Number(sizeParam);

  if (!Number.isInteger(page) || page < 0) {
    return {
      error: {
        code: "SOCIAL-400-PAGINATION",
        success: false,
        message: "Tham so pagination khong hop le.",
        data: null,
        errors: [{ field: "page", reason: "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  if (!Number.isInteger(size) || size < 1 || size > 50) {
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

export const socialSavedPostsHandlers = [
  http.get("*/api/v1/social/posts/saved", async ({ request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const url = new URL(request.url);
    const pagination = parsePagination(url);
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    if (url.searchParams.get("scenario") === "empty") {
      const empty = {
        items: [],
        meta: {
          page: pagination.page,
          size: pagination.size,
          totalElements: 0,
          totalPages: 0,
          hasNext: false,
        },
      };
      return HttpResponse.json(apiSuccess(200, "Lay danh sach bai da luu thanh cong.", empty), {
        status: 200,
      });
    }

    const data = buildSavedPostsPage(user.id, pagination);
    return HttpResponse.json(apiSuccess(200, "Lay danh sach bai da luu thanh cong.", data), {
      status: 200,
    });
  }),

  http.post("*/api/v1/social/posts/:postId/save", async ({ params, request }) => {
    await delay(300);

    const user = getUserByToken(request);
    const writeBlock = checkSocialMockUserCanWrite(user);
    if (writeBlock) {
      return HttpResponse.json(writeBlock.body, { status: writeBlock.status });
    }

    const postId = params.postId;
    if (!isValidObjectId(postId)) {
      return HttpResponse.json(apiError(400, "postId khong hop le."), { status: 400 });
    }

    const post = findFeedPost(postId);
    if (!post || (post.status || "ACTIVE") === "DELETED") {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const result = toggleUserSave(user.id, postId);
    const message = result.saved ? "Luu bai viet thanh cong." : "Bo luu bai viet thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result), { status: 200 });
  }),
];
