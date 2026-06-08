const ADMIN_ERROR_MESSAGES = {
  "ADMIN-401": "Phien dang nhap khong hop le.",
  "ADMIN-403": "Ban khong co quyen thuc hien thao tac nay.",
  "ADMIN-400-VALIDATION": "Du lieu khong hop le.",
  "ADMIN-400-PAGINATION": "Tham so phan trang khong hop le.",
  "ADMIN-404": "Khong tim thay du lieu.",
};

export function mapContentModerationApiError(error, fallback = "Co loi xay ra. Vui long thu lai.") {
  const code = String(error?.code ?? "");
  return ADMIN_ERROR_MESSAGES[code] || error?.message || fallback;
}

export function isAdminUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("ADMIN-401");
}