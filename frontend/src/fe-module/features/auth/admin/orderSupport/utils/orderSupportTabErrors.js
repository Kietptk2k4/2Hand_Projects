const RELOGIN_HINT =
  "Vui lòng đăng xuất và đăng nhập lại sau khi được cấp quyền, hoặc liên hệ Super Admin.";

export function isSupportForbiddenError(error) {
  const code = error?.code;
  return code === 403 || code === "403";
}

export function isSupportUnavailableError(error) {
  const code = error?.code;
  return code === 503 || code === "503";
}

function extractPermissionCode(message = "") {
  const match = String(message).match(/Missing permission:\s*(\S+)/i);
  return match?.[1] || "";
}

export function resolveSupportForbiddenMessage(error, { permissionCode, actionLabel } = {}) {
  const raw = (error?.message || "").trim();
  const code = permissionCode || extractPermissionCode(raw);
  const label = actionLabel || code || "truy cập";

  if (raw.toLowerCase().includes("missing permission") || code) {
    return `Bạn thiếu quyền ${code || label}. ${RELOGIN_HINT}`;
  }

  return raw || `Bạn không có quyền ${label}. ${RELOGIN_HINT}`;
}

export function handleSupportLoadError(
  error,
  {
    showSessionExpired,
    setStatus,
    setErrorMessage,
    permissionCode,
    actionLabel,
    fallbackMessage,
    notFoundMessage,
    unavailableMessage,
    preserveStatusOnForbidden = false,
    preserveStatusOnError = false,
  },
) {
  if (error?.code === 401) {
    showSessionExpired(error?.message);
    return;
  }

  if (isSupportForbiddenError(error)) {
    if (!preserveStatusOnForbidden) {
      setStatus("forbidden");
    }
    setErrorMessage(resolveSupportForbiddenMessage(error, { permissionCode, actionLabel }));
    return;
  }

  if (isSupportUnavailableError(error)) {
    if (!preserveStatusOnError) {
      setStatus("unavailable");
    }
    setErrorMessage(unavailableMessage || error?.message || fallbackMessage);
    return;
  }

  if (error?.code === 404 || error?.code === "404") {
    if (!preserveStatusOnError) {
      setStatus("error");
    }
    setErrorMessage(notFoundMessage || "Không tìm thấy dữ liệu.");
    return;
  }

  if (!preserveStatusOnError) {
    setStatus("error");
  }
  setErrorMessage(error?.message || fallbackMessage);
}