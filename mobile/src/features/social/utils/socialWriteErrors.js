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

  return {
    type: "generic",
    message: mapped.message || "Co loi xay ra. Vui long thu lai.",
    raw: mapped,
  };
}
