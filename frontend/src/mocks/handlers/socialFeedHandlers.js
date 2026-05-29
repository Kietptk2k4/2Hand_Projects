import { delay, http, HttpResponse } from "msw";
import { mockFollowingFeedPosts, mockGlobalFeedPosts } from "../data/socialFeedData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function parsePagination(url) {
  const pageParam = url.searchParams.get("page");
  const sizeParam = url.searchParams.get("size");

  const page = pageParam === null || pageParam === "" ? 0 : Number(pageParam);
  const size = sizeParam === null || sizeParam === "" ? 20 : Number(sizeParam);

  if (!Number.isInteger(page) || page < 0) {
    return {
      error: apiError(400, "Tham so pagination khong hop le.", [
        { field: "page", reason: "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0" },
      ]),
    };
  }

  if (!Number.isInteger(size) || size < 1 || size > 50) {
    return {
      error: apiError(400, "Tham so pagination khong hop le.", [
        { field: "size", reason: "MUST_BE_BETWEEN_1_AND_50" },
      ]),
    };
  }

  return { page, size };
}

function paginateFeed(items, page, size) {
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

async function handleFeedRequest(request, items, successMessage) {
  await delay(400);

  const user = getUserByToken(request);
  if (!user) {
    return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
  }

  const pagination = parsePagination(new URL(request.url));
  if (pagination.error) {
    return HttpResponse.json(pagination.error, { status: 400 });
  }

  const { page, size } = pagination;
  const data = paginateFeed(items, page, size);

  return HttpResponse.json(apiSuccess(200, successMessage, data), { status: 200 });
}

export const socialFeedHandlers = [
  http.get("*/api/v1/social/feed/global", (ctx) =>
    handleFeedRequest(ctx.request, mockGlobalFeedPosts, "Lay global feed thanh cong.")
  ),

  http.get("*/api/v1/social/feed/following", (ctx) =>
    handleFeedRequest(ctx.request, mockFollowingFeedPosts, "Lay following feed thanh cong.")
  ),
];
