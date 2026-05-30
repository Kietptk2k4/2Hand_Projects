import { delay, http, HttpResponse } from "msw";
import {
  buildSocialProfile,
  buildUserPostsPage,
  MOCK_SOCIAL_USER_IDS,
} from "../data/socialProfileData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function isValidUuid(value) {
  return UUID_REGEX.test(value || "");
}

function parsePostsPagination(url) {
  const pageParam = url.searchParams.get("page");
  const sizeParam = url.searchParams.get("size");
  const statusFilter = url.searchParams.get("status_filter") || "published";

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

  if (!["published", "all"].includes(statusFilter)) {
    return {
      error: {
        code: "SOCIAL-400-PAGINATION",
        success: false,
        message: "Tham so pagination khong hop le.",
        data: null,
        errors: [{ field: "status_filter", reason: "INVALID" }],
        timestamp: new Date().toISOString(),
      },
    };
  }

  return { page, size, statusFilter };
}

export const socialProfileHandlers = [
  http.get("*/api/v1/social/users/:userId/profile", async ({ params, request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(apiError(400, "userId khong hop le."), { status: 400 });
    }

    if (userId === MOCK_SOCIAL_USER_IDS.NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    const profile = buildSocialProfile(userId, user.id);
    if (!profile) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    return HttpResponse.json(apiSuccess(200, "Lay social profile thanh cong.", profile), {
      status: 200,
    });
  }),

  http.get("*/api/v1/social/users/:userId/posts", async ({ params, request }) => {
    await delay(450);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const userId = params.userId;
    if (!isValidUuid(userId)) {
      return HttpResponse.json(apiError(400, "userId khong hop le."), { status: 400 });
    }

    if (userId === MOCK_SOCIAL_USER_IDS.NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    const pagination = parsePostsPagination(new URL(request.url));
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    const result = buildUserPostsPage(userId, user.id, pagination);
    if (result.error === 404) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }
    if (result.error === 403) {
      return HttpResponse.json(
        apiError(403, "Ban khong co quyen xem bai viet cua nguoi dung nay."),
        { status: 403 }
      );
    }
    if (result.error === 400) {
      return HttpResponse.json(
        {
          code: result.code || "SOCIAL-400-PAGINATION",
          success: false,
          message: "status_filter=all chi danh cho chu profile.",
          data: null,
          errors: [{ field: "status_filter", reason: "OWNER_ONLY" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach bai viet thanh cong.", {
        items: result.items,
        meta: result.meta,
      }),
      { status: 200 }
    );
  }),
];
