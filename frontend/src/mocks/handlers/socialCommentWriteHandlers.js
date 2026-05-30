import { delay, http, HttpResponse } from "msw";
import {
  appendReplyComment,
  appendTopLevelComment,
  findCommentById,
  isReplyCommentId,
  softDeleteComment,
} from "../data/socialCommentsData";
import {
  decrementPostReplyCount,
  findFeedPost,
  incrementPostReplyCount,
  MOCK_POST_ID_NOT_FOUND,
} from "../data/socialPostDetailData";
import { getUserByToken, isValidObjectId } from "../utils/socialMockAuth";
import { checkSocialMockUserCanWrite } from "../utils/socialMockWriteGuard";
import { apiError, apiSuccess } from "../utils/response";

const MAX_COMMENT_LENGTH = 2000;

function validateContentText(body) {
  const contentText = body?.contentText;
  if (typeof contentText !== "string") {
    return {
      error: apiError(400, "Du lieu khong hop le.", [
        { field: "contentText", reason: "MUST_NOT_BE_BLANK" },
      ]),
    };
  }
  const trimmed = contentText.trim();
  if (!trimmed) {
    return {
      error: apiError(400, "Du lieu khong hop le.", [
        { field: "contentText", reason: "MUST_NOT_BE_BLANK" },
      ]),
    };
  }
  if (trimmed.length > MAX_COMMENT_LENGTH) {
    return {
      error: apiError(400, "Du lieu khong hop le.", [
        { field: "contentText", reason: "MAX_LENGTH_2000" },
      ]),
    };
  }
  return { value: trimmed };
}

function checkUserCanWrite(user) {
  const blocked = checkSocialMockUserCanWrite(user);
  if (blocked) return blocked;
  return { status: 200 };
}

function isModeratorUser(user) {
  return Boolean(user?.is_admin);
}

function checkPostAllowsComments(postId) {
  if (postId === MOCK_POST_ID_NOT_FOUND) {
    return { status: 404, message: "Khong tim thay bai viet." };
  }
  const feedPost = findFeedPost(postId);
  if (!feedPost) {
    return { status: 404, message: "Khong tim thay bai viet." };
  }
  if ((feedPost.status || "ACTIVE") !== "ACTIVE") {
    return { status: 403, message: "Khong the binh luan tren bai viet nay." };
  }
  if (feedPost.allowComments === false) {
    return { status: 403, message: "Bai viet da tat binh luan." };
  }
  return { status: 200, post: feedPost };
}

export const socialCommentWriteHandlers = [
  http.post("*/api/v1/social/posts/:postId/comments", async ({ params, request }) => {
    await delay(350);

    const user = getUserByToken(request);
    const userCheck = checkUserCanWrite(user);
    if (userCheck.status === 401) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (userCheck.status === 403) {
      return HttpResponse.json(userCheck.body, { status: 403 });
    }

    const postId = params.postId;
    if (!isValidObjectId(postId)) {
      return HttpResponse.json(apiError(400, "postId khong hop le."), { status: 400 });
    }

    let body;
    try {
      body = await request.json();
    } catch {
      body = {};
    }

    const contentValidation = validateContentText(body);
    if (contentValidation.error) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: contentValidation.error.message,
          data: null,
          errors: contentValidation.error.errors,
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const postCheck = checkPostAllowsComments(postId);
    if (postCheck.status === 404) {
      return HttpResponse.json(apiError(404, postCheck.message), { status: 404 });
    }
    if (postCheck.status === 403) {
      return HttpResponse.json(apiError(403, postCheck.message), { status: 403 });
    }

    const data = appendTopLevelComment(postId, user.id, contentValidation.value);
    incrementPostReplyCount(postId, 1);

    return HttpResponse.json(apiSuccess(201, "Tao binh luan thanh cong.", data), { status: 201 });
  }),

  http.post("*/api/v1/social/comments/:commentId/replies", async ({ params, request }) => {
    await delay(350);

    const user = getUserByToken(request);
    const userCheck = checkUserCanWrite(user);
    if (userCheck.status === 401) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (userCheck.status === 403) {
      return HttpResponse.json(userCheck.body, { status: 403 });
    }

    const commentId = params.commentId;
    if (!isValidObjectId(commentId)) {
      return HttpResponse.json(apiError(400, "commentId khong hop le."), { status: 400 });
    }

    if (isReplyCommentId(commentId)) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: "Du lieu khong hop le.",
          data: null,
          errors: [{ field: "parentCommentId", reason: "NESTED_REPLY_NOT_ALLOWED" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const parentInfo = findCommentById(commentId);
    if (!parentInfo) {
      return HttpResponse.json(apiError(404, "Khong tim thay binh luan."), { status: 404 });
    }
    if (!parentInfo.isTopLevel) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: "Du lieu khong hop le.",
          data: null,
          errors: [{ field: "parentCommentId", reason: "NESTED_REPLY_NOT_ALLOWED" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const postCheck = checkPostAllowsComments(parentInfo.comment.postId);
    if (postCheck.status === 404) {
      return HttpResponse.json(apiError(404, postCheck.message), { status: 404 });
    }
    if (postCheck.status === 403) {
      return HttpResponse.json(apiError(403, postCheck.message), { status: 403 });
    }

    let body;
    try {
      body = await request.json();
    } catch {
      body = {};
    }

    const contentValidation = validateContentText(body);
    if (contentValidation.error) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: contentValidation.error.message,
          data: null,
          errors: contentValidation.error.errors,
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const result = appendReplyComment(commentId, user.id, contentValidation.value);
    if (result.error) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: "Du lieu khong hop le.",
          data: null,
          errors: [{ field: "parentCommentId", reason: "NESTED_REPLY_NOT_ALLOWED" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    incrementPostReplyCount(parentInfo.comment.postId, 1);

    return HttpResponse.json(apiSuccess(201, "Tra loi comment thanh cong.", result), {
      status: 201,
    });
  }),

  http.delete("*/api/v1/social/comments/:commentId", async ({ params, request }) => {
    await delay(300);

    const user = getUserByToken(request);
    const userCheck = checkUserCanWrite(user);
    if (userCheck.status === 401) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (userCheck.status === 403) {
      return HttpResponse.json(userCheck.body, { status: 403 });
    }

    const commentId = params.commentId;
    if (!isValidObjectId(commentId)) {
      return HttpResponse.json(apiError(400, "commentId khong hop le."), { status: 400 });
    }

    const result = softDeleteComment(commentId, user.id, {
      isModerator: isModeratorUser(user),
    });

    if (result.error === "NOT_FOUND") {
      return HttpResponse.json(apiError(404, "Khong tim thay binh luan."), { status: 404 });
    }
    if (result.error === "FORBIDDEN") {
      return HttpResponse.json(apiError(403, "Ban khong co quyen xoa binh luan nay."), {
        status: 403,
      });
    }

    if (result.shouldDecrementPost) {
      decrementPostReplyCount(result.postId, 1);
    }

    return HttpResponse.json(apiSuccess(200, "Xoa comment thanh cong.", result.data), {
      status: 200,
    });
  }),
];
