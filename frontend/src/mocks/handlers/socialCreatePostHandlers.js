import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import { mockGlobalFeedPosts, mockPostId } from "../data/socialFeedData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp", "video/mp4"];
const IMAGE_MAX = 10 * 1024 * 1024;
const VIDEO_MAX = 100 * 1024 * 1024;

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function generateNewPostId() {
  const suffix = crypto.randomUUID().replace(/-/g, "").slice(0, 4);
  const id = `674a10000000000000000${suffix}`;
  return id.length === 24 ? id : mockPostId("99");
}

function validateCreateBody(body) {
  const errors = [];

  if (!body?.visibility || !["PUBLIC", "FOLLOWERS"].includes(body.visibility)) {
    errors.push({ field: "visibility", reason: "Visibility chi chap nhan PUBLIC hoac FOLLOWERS." });
  }

  if (body?.caption && body.caption.length > 2000) {
    errors.push({ field: "caption", reason: "Caption vuot qua 2000 ky tu." });
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

  if (!body?.caption?.trim() && (!body?.media || body.media.length === 0)) {
    errors.push({ field: "caption", reason: "Can caption hoac media." });
  }

  return errors;
}

export const socialCreatePostHandlers = [
  http.post("*/api/v1/social/posts/media/upload-url", async ({ request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const body = await request.json();
    const contentType = body?.content_type;
    const fileSize = body?.file_size_bytes;
    const mediaKind = body?.media_kind;

    if (!ALLOWED_TYPES.includes(contentType)) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "content_type", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    if (!mediaKind || !["IMAGE", "VIDEO"].includes(mediaKind)) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "media_kind", reason: "INVALID" }]),
        { status: 400 }
      );
    }

    const maxSize = mediaKind === "VIDEO" ? VIDEO_MAX : IMAGE_MAX;
    if (!fileSize || fileSize > maxSize) {
      return HttpResponse.json(
        apiError(400, "Validation failed", [{ field: "file_size_bytes", reason: "TOO_LARGE" }]),
        { status: 400 }
      );
    }

    const ext = contentType.split("/")[1] === "jpeg" ? "jpg" : contentType.split("/")[1];
    const objectKey = `posts/${user.id}/${crypto.randomUUID()}.${ext}`;
    const mediaUrl = `https://cdn.2hands.vn/${objectKey}`;

    return HttpResponse.json(
      apiSuccess(200, "Tao link upload media thanh cong.", {
        upload_url: `https://mock-minio.2hands.vn/upload/${objectKey}`,
        object_key: objectKey,
        media_url: mediaUrl,
        media_kind: mediaKind,
        expires_at: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
        max_file_size_bytes: maxSize,
        allowed_content_types: ALLOWED_TYPES,
      }),
      { status: 200 }
    );
  }),

  http.post("*/api/v1/social/posts", async ({ request }) => {
    await delay(500);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const body = await request.json();
    const validationErrors = validateCreateBody(body);
    if (validationErrors.length > 0) {
      return HttpResponse.json(
        {
          code: 400,
          success: false,
          message: "Validation failed",
          data: null,
          errors: validationErrors,
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const publish = Boolean(body.publish);
    const status = publish ? "ACTIVE" : "DRAFT";
    const postId = generateNewPostId();
    const now = new Date().toISOString();

    const created = {
      postId,
      authorId: user.id,
      caption: body.caption || "",
      media: body.media || [],
      productTags: body.productTags || [],
      status,
      visibility: body.visibility,
      allowComments: body.allowComments !== false,
      hashtags: body.hashtags || [],
      likeCount: 0,
      replyCount: 0,
      createdAt: now,
      updatedAt: now,
    };

    if (publish && status === "ACTIVE" && body.visibility === "PUBLIC") {
      mockGlobalFeedPosts.unshift({
        postId: created.postId,
        authorId: created.authorId,
        caption: created.caption,
        media: created.media,
        visibility: created.visibility,
        likeCount: 0,
        replyCount: 0,
        hashtags: created.hashtags,
        allowComments: created.allowComments,
        createdAt: created.createdAt,
        updatedAt: created.updatedAt,
      });
    }

    return HttpResponse.json(apiSuccess(201, "Tao bai viet thanh cong.", created), {
      status: 201,
    });
  }),
];
