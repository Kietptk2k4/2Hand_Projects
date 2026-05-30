import { delay, http, HttpResponse } from "msw";
import {
  applyMockFollow,
  applyMockUnfollow,
  buildSocialProfile,
  getMockFollowRelation,
  MOCK_SOCIAL_USER_IDS,
} from "../data/socialProfileData";
import { getUserByToken } from "../utils/socialMockAuth";
import { checkSocialMockUserCanWrite } from "../utils/socialMockWriteGuard";
import { apiError, apiSuccess } from "../utils/response";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function isValidUuid(value) {
  return UUID_REGEX.test(value || "");
}

export const socialFollowHandlers = [
  http.post("*/api/v1/social/users/:userId/follow", async ({ params, request }) => {
    await delay(350);

    const user = getUserByToken(request);
    const writeBlock = checkSocialMockUserCanWrite(user);
    if (writeBlock) {
      return HttpResponse.json(writeBlock.body, { status: writeBlock.status });
    }

    const followeeId = params.userId;
    if (!isValidUuid(followeeId)) {
      return HttpResponse.json(apiError(400, "userId khong hop le."), { status: 400 });
    }

    if (followeeId === user.id) {
      return HttpResponse.json(
        {
          code: "SOCIAL-400",
          success: false,
          message: "Khong the tu theo doi chinh minh.",
          data: null,
          errors: [{ field: "userId", reason: "SELF_FOLLOW" }],
          timestamp: new Date().toISOString(),
        },
        { status: 400 }
      );
    }

    if (followeeId === MOCK_SOCIAL_USER_IDS.NOT_FOUND) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    const targetProfile = buildSocialProfile(followeeId, user.id);
    if (!targetProfile) {
      return HttpResponse.json(apiError(404, "Khong tim thay nguoi dung."), { status: 404 });
    }

    const existing = getMockFollowRelation(user.id, followeeId);
    if (existing) {
      return HttpResponse.json(
        apiSuccess(200, "Theo doi nguoi dung thanh cong.", {
          followeeId,
          status: existing.status,
          createdAt: existing.createdAt,
        }),
        { status: 200 }
      );
    }

    const status = targetProfile.isPrivate ? "PENDING" : "ACCEPTED";
    const createdAt = new Date().toISOString();
    applyMockFollow(user.id, followeeId, status, createdAt);

    return HttpResponse.json(
      apiSuccess(201, "Theo doi nguoi dung thanh cong.", {
        followeeId,
        status,
        createdAt,
      }),
      { status: 201 }
    );
  }),

  http.delete("*/api/v1/social/users/:userId/follow", async ({ params, request }) => {
    await delay(300);

    const user = getUserByToken(request);
    const writeBlock = checkSocialMockUserCanWrite(user);
    if (writeBlock) {
      return HttpResponse.json(writeBlock.body, { status: writeBlock.status });
    }

    const followeeId = params.userId;
    if (!isValidUuid(followeeId)) {
      return HttpResponse.json(apiError(400, "userId khong hop le."), { status: 400 });
    }

    const wasFollowing = applyMockUnfollow(user.id, followeeId);

    return HttpResponse.json(
      apiSuccess(200, "Huy theo doi nguoi dung thanh cong.", {
        followeeId,
        wasFollowing,
      }),
      { status: 200 }
    );
  }),
];
