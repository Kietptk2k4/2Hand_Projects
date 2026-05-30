import { delay, http, HttpResponse } from "msw";
import { buildSearchPostsPage } from "../data/socialSearchPostsData";
import { getUserByToken } from "../utils/socialMockAuth";
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

function validateQuery(url) {
  const qParam = url.searchParams.get("q");
  if (qParam === null) {
    return {
      error: {
        code: "SOCIAL-400",
        success: false,
        message: "Tu khoa tim kiem khong hop le.",
        data: null,
        errors: [{ field: "q", reason: "MUST_NOT_BE_BLANK" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  const trimmed = qParam.trim();
  if (!trimmed) {
    return {
      error: {
        code: "SOCIAL-400",
        success: false,
        message: "Tu khoa tim kiem khong hop le.",
        data: null,
        errors: [{ field: "q", reason: "MUST_NOT_BE_BLANK" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  if (trimmed.length > 255) {
    return {
      error: {
        code: "SOCIAL-400",
        success: false,
        message: "Tu khoa tim kiem khong hop le.",
        data: null,
        errors: [{ field: "q", reason: "MAX_LENGTH_255" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  return { q: trimmed };
}

export const socialSearchPostsHandlers = [
  http.get("*/api/v1/social/search/posts", async ({ request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const url = new URL(request.url);
    const queryValidation = validateQuery(url);
    if (queryValidation.error) {
      return HttpResponse.json(queryValidation.error, { status: 400 });
    }

    const pagination = parsePagination(url);
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    const data = buildSearchPostsPage(queryValidation.q, pagination);
    return HttpResponse.json(apiSuccess(200, "Tim kiem bai viet thanh cong.", data), {
      status: 200,
    });
  }),
];
