export const SHOW_PASSWORD = "Hien";
export const HIDE_PASSWORD = "An";

export const SESSION_EXPIRED_TITLE = "Phien dang nhap da het han";
export const SESSION_EXPIRED_DEFAULT_MESSAGE = "Vui long dang nhap lai de tiep tuc.";
export const SESSION_EXPIRED_SIGN_IN = "Dang nhap";

export const GENERIC_ERROR_RETRY = "Co loi xay ra. Vui long thu lai.";
export const INVALID_FIELD_MESSAGE = "Truong du lieu khong hop le.";

export const SESSION_STATUS_LABELS = {
  ACTIVE: "Dang hoat dong",
  LOGGED_OUT: "Da dang xuat",
  REVOKED: "Da thu hoi",
  EXPIRED: "Het han",
};

export const LOGIN_METHOD_LABELS = {
  EMAIL: "Email",
  GOOGLE: "Google",
  FACEBOOK: "Facebook",
};

export function getSessionStatusLabel(status) {
  return SESSION_STATUS_LABELS[status] || status || SESSION_STATUS_LABELS.ACTIVE;
}
