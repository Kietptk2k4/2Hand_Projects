export const OAUTH_FAILURE_MESSAGES = {
  "AUTH-401-OAUTH-PROFILE-INVALID": "Xác thực Google/Facebook thất bại. Vui lòng thử lại.",
  "AUTH-401-OAUTH-SESSION-INVALID": "Phiên đăng nhập OAuth không hợp lệ hoặc đã hết hạn.",
  "AUTH-400-OAUTH-EMAIL-MISSING":
    "Tài khoản chưa cấp quyền email. Vui lòng cho phép email khi đăng nhập.",
  "AUTH-403-OAUTH-ACCOUNT-UNAVAILABLE": "Tài khoản hiện không khả dụng.",
  "AUTH-500": "Hệ thống đang bận. Vui lòng thử lại sau.",
};

export function mapOAuthFailureMessage(code, fallbackMessage) {
  if (!code) return fallbackMessage || "Đăng nhập OAuth thất bại. Vui lòng thử lại.";
  return OAUTH_FAILURE_MESSAGES[code] || fallbackMessage || "Đăng nhập OAuth thất bại. Vui lòng thử lại.";
}
