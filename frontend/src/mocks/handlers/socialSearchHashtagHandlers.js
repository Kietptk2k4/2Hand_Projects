import { delay, http, HttpResponse } from "msw";
import { buildHashtagPostsPage } from "../data/socialSearchHashtagData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const HASHTAG_PATTERN = /^[a-zA-Z0-9_]{1,100}$/;

function normalizeHashtag(raw) {
  return decodeURIComponent(String(raw || ""))
    .replace(/^#+/, "")
    .trim();
}

function validateHashtag(raw) {
  const normalized = normalizeHashtag(raw);
  if (!normalized) {
    return {
      error: {
        code: "SOCIAL-400",
        success: false,
        message: "Hashtag khong hop le.",
        data: null,
        errors: [{ field: "hashtag", reason: "MUST_NOT_BE_BLANK" }],
        timestamp: new Date().toISOString(),
      },
    };
  }
  if (!HASHTAG_PATTERN.test(normalized)) {
    return {
      error: {
        code: "SOCIAL-400",
        success: false,
        message: "Hashtag khong hop le.",
        data: null,
        errors: [{ field: "hashtag", reason: "INVALID_FORMAT" }],
        timestamp: new Date().toISOString(),
      },
    };
  }
  return { hashtag: normalized };
}

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

export const socialSearchHashtagHandlers = [
  http.get("*/api/v1/social/search/hashtags/:hashtag", async ({ params, request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const hashtagValidation = validateHashtag(params.hashtag);
    if (hashtagValidation.error) {
      return HttpResponse.json(hashtagValidation.error, { status: 400 });
    }

    const pagination = parsePagination(new URL(request.url));
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    const data = buildHashtagPostsPage(hashtagValidation.hashtag, pagination);
    return HttpResponse.json(apiSuccess(200, "Tim kiem hashtag thanh cong.", data), {
      status: 200,
    });
  }),
];
