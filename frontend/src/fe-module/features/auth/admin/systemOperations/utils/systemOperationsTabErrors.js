const RELOGIN_HINT =
  "Vui lòng đăng xuất và đăng nhập lại sau khi được cấp quyền, hoặc liên hệ Super Admin.";

export function isForbiddenError(error) {
  const code = String(error?.code ?? "");
  return code === "403" || code.includes("403") || code.includes("ADMIN-403");
}

export function resolveForbiddenMessage(error, permissionHint) {
  const raw = (error?.message || "").trim();
  if (raw.toLowerCase().includes("missing permission")) {
    return `${raw} ${RELOGIN_HINT}`;
  }
  return raw || `Bạn không có quyền ${permissionHint}. ${RELOGIN_HINT}`;
}

export function handleSystemOperationsLoadError(
  error,
  { showSessionExpired, setStatus, setErrorMessage, notFoundMessage, permissionHint },
) {
  if (String(error?.code ?? "").includes("401")) {
    showSessionExpired(error?.message);
    return;
  }

  if (isForbiddenError(error)) {
    setStatus("forbidden");
    setErrorMessage(resolveForbiddenMessage(error, permissionHint));
    return;
  }

  if (String(error?.code ?? "").includes("404")) {
    setStatus("error");
    setErrorMessage(notFoundMessage || "Không tìm thấy dữ liệu.");
    return;
  }

  setStatus("error");
  setErrorMessage(error?.message || "Không thể tải dữ liệu.");
}