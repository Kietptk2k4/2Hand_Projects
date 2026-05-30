import { delay, http, HttpResponse } from "msw";
import {
  buildUserRelationsPage,
  canViewerAccessRelations,
} from "../data/socialRelationsData";
import { MOCK_SOCIAL_USER_IDS } from "../../fe-module/features/social/constants/socialProfileConstants";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

export const socialRelationsHandlers = [
  http.get("*/api/v1/social/users/:userId/relations", async ({ params, request }) => {
    await delay(400);

    const user = getUserByToken(request);
    if (!user) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }

    const targetUserId = params.userId;
    if (targetUserId === MOCK_SOCIAL_USER_IDS.NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    const url = new URL(request.url);
    const type = url.searchParams.get("type");
    const pageParam = url.searchParams.get("page");
    const sizeParam = url.searchParams.get("size");

    if (!type || !["followers", "following"].includes(type)) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400",
          success: false,
          message: "Tham so type khong hop le.",
          data: null,
          errors: [{ field: "type", reason: "MUST_BE_followers_OR_following" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const page = pageParam === null || pageParam === "" ? 0 : Number(pageParam);
    const size = sizeParam === null || sizeParam === "" ? 20 : Number(sizeParam);

    if (!Number.isInteger(page) || page < 0) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-PAGINATION",
          success: false,
          message: "Tham so pagination khong hop le.",
          data: null,
          errors: [{ field: "page", reason: "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    if (!Number.isInteger(size) || size < 1 || size > 50) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400-PAGINATION",
          success: false,
          message: "Tham so pagination khong hop le.",
          data: null,
          errors: [{ field: "size", reason: "MUST_BE_BETWEEN_1_AND_50" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    const access = canViewerAccessRelations(targetUserId, user.id);
    if (!access.ok) {
      if (access.status === 404) {
        return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
      }
      return HttpResponse.json(
        apiError(403, "Ban khong co quyen xem danh sach quan he nay."),
        { status: 403 }
      );
    }

    const result = buildUserRelationsPage(targetUserId, user.id, { type, page, size });
    if (result.error) {
      return HttpResponse.json(apiError(result.error, "Loi truy van quan he."), {
        status: result.error,
      });
    }

    return HttpResponse.json(apiSuccess(200, "Lay danh sach quan he thanh cong.", result), {
      status: 200,
    });
  }),
];
