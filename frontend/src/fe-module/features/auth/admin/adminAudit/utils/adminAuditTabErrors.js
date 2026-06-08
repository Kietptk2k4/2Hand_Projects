const RELOGIN_HINT =
  "Vui long dang xuat va dang nhap lai sau khi duoc cap quyen, hoac lien he Super Admin.";

export function isAuditForbiddenError(error) {
  const code = String(error?.code ?? "");
  return code === "403" || code.includes("403") || code.includes("ADMIN-403");
}

export function resolveAuditForbiddenMessage(error) {
  const raw = (error?.message || "").trim();
  if (raw.toLowerCase().includes("missing permission") || raw.includes("ADMIN_AUDIT")) {
    return `Ban thieu quyen ADMIN_AUDIT_VIEW. ${RELOGIN_HINT}`;
  }
  return raw || `Ban khong co quyen xem nhat ky kiem toan. ${RELOGIN_HINT}`;
}

export function handleAuditLoadError(
  error,
  { showSessionExpired, setStatus, setErrorMessage, notFoundMessage, preserveStatusOnForbidden = false },
) {
  if (String(error?.code ?? "") === "401" || String(error?.code ?? "").includes("401")) {
    showSessionExpired(error?.message);
    return;
  }

  if (isAuditForbiddenError(error)) {
    if (!preserveStatusOnForbidden) setStatus("forbidden");
    setErrorMessage(resolveAuditForbiddenMessage(error));
    return;
  }

  if (String(error?.code ?? "") === "404" || String(error?.code ?? "").includes("404")) {
    setStatus("error");
    setErrorMessage(notFoundMessage || "Khong tim thay nhat ky hanh dong.");
    return;
  }

  setStatus("error");
  setErrorMessage(error?.message || "Khong the tai nhat ky hanh dong admin.");
}