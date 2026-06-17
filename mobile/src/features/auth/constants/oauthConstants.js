export const OAUTH_FAILURE_MESSAGES = {
  "AUTH-401-OAUTH-PROFILE-INVALID": "Xac thuc Google/Facebook that bai. Vui long thu lai.",
  "AUTH-401-OAUTH-SESSION-INVALID": "Phien dang nhap OAuth khong hop le hoac da het han.",
  "AUTH-400-OAUTH-EMAIL-MISSING":
    "Tai khoan chua cap quyen email. Vui long cho phep email khi dang nhap.",
  "AUTH-403-OAUTH-ACCOUNT-UNAVAILABLE": "Tai khoan hien khong kha dung.",
  "AUTH-500": "He thong dang ban. Vui long thu lai sau.",
};

export function mapOAuthFailureMessage(code, fallbackMessage) {
  if (!code) return fallbackMessage || "Dang nhap OAuth that bai. Vui long thu lai.";
  return OAUTH_FAILURE_MESSAGES[code] || fallbackMessage || "Dang nhap OAuth that bai. Vui long thu lai.";
}
