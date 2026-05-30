import { mapAxiosError } from "../api/socialApiResponse";

export const SOCIAL_WRITE_SUSPENDED_CODE = "SOCIAL-403-SUSPENDED";

export const DEFAULT_SUSPEND_MESSAGE =
  "Tài khoản bị đình chỉ, không thể thực hiện hành động này.";

export function isSuspendedWriteError(error) {
  const code = error?.code ?? error?.response?.data?.code;
  return (
    code === SOCIAL_WRITE_SUSPENDED_CODE || String(code || "").includes("SUSPENDED")
  );
}

export function mapSocialWriteError(error) {
  const mapped =
    error && typeof error === "object" && error.code !== undefined && !error.response
      ? error
      : mapAxiosError(error);

  const code = mapped?.code;

  if (code === 401 || code === "SOCIAL-401") {
    return { type: "session", message: mapped.message, raw: mapped };
  }

  if (isSuspendedWriteError(mapped)) {
    return {
      type: "suspended",
      message: mapped.message || DEFAULT_SUSPEND_MESSAGE,
      raw: mapped,
    };
  }

  if (code === 400 || String(code || "").includes("400")) {
    return {
      type: "validation",
      message: mapped.message || "Nội dung không hợp lệ.",
      raw: mapped,
    };
  }

  if (code === 403 || String(code || "").includes("403")) {
    return {
      type: "forbidden",
      message: mapped.message || "Không có quyền thực hiện hành động này.",
      raw: mapped,
    };
  }

  if (code === 404) {
    return {
      type: "notFound",
      message: mapped.message || "Không tìm thấy nội dung.",
      raw: mapped,
    };
  }

  return {
    type: "generic",
    message: mapped.message || "Có lỗi xảy ra. Vui lòng thử lại.",
    raw: mapped,
  };
}

/** @deprecated Use mapSocialWriteError — kept for comment hooks */
export function mapSubmitError(error) {
  return mapSocialWriteError(error);
}
