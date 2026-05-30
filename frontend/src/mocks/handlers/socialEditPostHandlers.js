import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import {
  buildPostDetail,
  findFeedPost,
  MOCK_POST_ID_NOT_FOUND,
  touchFeedPostUpdatedAt,
  updateFeedPostInStore,
} from "../data/socialPostDetailData";
import { getUserByToken, isValidObjectId } from "../utils/socialMockAuth";
import { checkSocialMockUserCanWrite } from "../utils/socialMockWriteGuard";
import { apiError, apiSuccess } from "../utils/response";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function validateEditBody(body) {
  const errors = [];

  if (body?.caption !== undefined && body.caption !== null && body.caption.length > 2000) {
    errors.push({ field: "caption", reason: "Caption vuot qua 2000 ky tu." });
  }

  if (body?.visibility !== undefined && !["PUBLIC", "FOLLOWERS"].includes(body.visibility)) {
    errors.push({ field: "visibility", reason: "Visibility chi chap nhan PUBLIC hoac FOLLOWERS." });
  }

  if (body?.media?.length > 10) {
    errors.push({ field: "media", reason: "Toi da 10 media." });
  }

  if (body?.hashtags?.length > 30) {
    errors.push({ field: "hashtags", reason: "Toi da 30 hashtag." });
  }

  if (body?.productTags?.length > 10) {
    errors.push({ field: "productTags", reason: "Toi da 10 product tag." });
  }

  body?.productTags?.forEach((tag, index) => {
    if (!UUID_REGEX.test(tag?.productId || "")) {
      errors.push({
        field: `productTags[${index}].productId`,
        reason: "productId phai la UUID.",
      });
    }
    if (tag?.price !== undefined && Number(tag.price) < 0) {
      errors.push({
        field: `productTags[${index}].price`,
        reason: "price phai >= 0.",
      });
    }
  });

  body?.media?.forEach((item, index) => {
    if (!item?.url || !["IMAGE", "VIDEO"].includes(item?.type)) {
      errors.push({ field: `media[${index}]`, reason: "Media khong hop le." });
    }
  });

  return errors;
}

function buildUpdatedResponse(postId, viewerUserId) {
  const detail = buildPostDetail(postId, viewerUserId);
  if (!detail) return null;

  const feedPost = findFeedPost(postId);
  return {
    postId: detail.postId,
    authorId: feedPost.authorId,
    caption: detail.caption,
    media: detail.media,
    productTags: detail.productTags,
    status: detail.status,
    visibility: detail.visibility,
    allowComments: detail.allowComments,
    hashtags: detail.hashtags,
    createdAt: detail.createdAt,
    updatedAt: feedPost.updatedAt,
  };
}

export const socialEditPostHandlers = [
  http.put("*/api/v1/social/posts/:postId", async ({ params, request }) => {
    await delay(450);

    const user = getUserByToken(request);
    const writeBlock = checkSocialMockUserCanWrite(user);
    if (writeBlock) {
      return HttpResponse.json(writeBlock.body, { status: writeBlock.status });
    }

    const postId = params.postId;
    if (!isValidObjectId(postId)) {
      return HttpResponse.json(apiError(400, "postId khong hop le."), { status: 400 });
    }

    if (postId === MOCK_POST_ID_NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    const feedPost = findFeedPost(postId);
    if (!feedPost) {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    if ((feedPost.status || "ACTIVE") === "DELETED") {
      return HttpResponse.json(apiError(404, "Khong tim thay bai viet."), { status: 404 });
    }

    if (feedPost.authorId !== user.id) {
      return HttpResponse.json(apiError(403, "Ban khong phai tac gia bai viet."), { status: 403 });
    }

    const body = await request.json();
    const validationErrors = validateEditBody(body);
    if (validationErrors.length > 0) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-VALIDATION",
          success: false,
          message: "Validation failed",
          data: null,
          errors: validationErrors,
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const patch = {};
    if (body?.caption !== undefined) patch.caption = body.caption;
    if (body?.media !== undefined) patch.media = body.media;
    if (body?.hashtags !== undefined) patch.hashtags = body.hashtags;
    if (body?.visibility !== undefined) patch.visibility = body.visibility;
    if (body?.allowComments !== undefined) patch.allowComments = body.allowComments;
    if (body?.productTags !== undefined) patch.productTags = body.productTags;

    if (Object.keys(patch).length === 0) {
      touchFeedPostUpdatedAt(postId);
    } else {
      updateFeedPostInStore(postId, patch);
    }

    const data = buildUpdatedResponse(postId, user.id);
    return HttpResponse.json(apiSuccess(200, "Cap nhat bai viet thanh cong.", data), {
      status: 200,
    });
  }),
];
