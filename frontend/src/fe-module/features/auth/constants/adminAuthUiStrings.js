export const ADMIN_PORTAL_TITLE = "2Hands Admin Portal";
export const ADMIN_PORTAL_SUBTITLE = "Secure management access";

export const ADMIN_LOGIN_SUCCESS_REDIRECT = "/admin";
export const ADMIN_LOGOUT_SUCCESS_MESSAGE = "Da dang xuat khoi admin portal.";

export const ADMIN_LOGIN_ERROR_BY_CODE = {
  401: "Email hoac mat khau khong chinh xac.",
  403: "Tai khoan khong co quyen truy cap admin portal hoac da bi dinh chi.",
  429: "Ban dang thu qua nhieu lan. Vui long doi it phut roi thu lai.",
  503: "He thong xac thuc tam khong kha dung. Vui long thu lai sau.",
};

export const ADMIN_LOGOUT_FALLBACK_MESSAGE =
  "Da dang xuat tren thiet bi nay. Neu can, vui long thu lai.";

export const ADMIN_ACCESS_DENIED_MESSAGE =
  "Ban khong co quyen truy cap admin portal. Vui long dang nhap bang tai khoan admin.";