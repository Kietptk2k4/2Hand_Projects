import { apiError } from "./response";

export const SOCIAL_SUSPENDED_WRITE_MESSAGE =
  "Tai khoan bi dinh chi, khong the thuc hien hanh dong nay.";

export function socialSuspendedWriteBody() {
  return {
    code: "SOCIAL-403-SUSPENDED",
    success: false,
    message: SOCIAL_SUSPENDED_WRITE_MESSAGE,
    data: null,
    errors: null,
    timestamp: new Date().toISOString(),
  };
}

/**
 * @returns {{ status: number, body: object } | null} block response, or null if allowed
 */
export function checkSocialMockUserCanWrite(user) {
  if (!user) {
    return { status: 401, body: apiError(401, "Authentication required") };
  }

  if (user.status === "SUSPENDED") {
    return { status: 403, body: socialSuspendedWriteBody() };
  }

  if (user.status === "DELETED") {
    return {
      status: 403,
      body: {
        code: "SOCIAL-403",
        success: false,
        message: "Access denied",
        data: null,
        errors: null,
        timestamp: new Date().toISOString(),
      },
    };
  }

  return null;
}
