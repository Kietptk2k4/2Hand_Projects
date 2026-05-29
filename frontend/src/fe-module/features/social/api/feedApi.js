import { socialApiClient } from "../../../services/http/socialApiClient";

function normalizeErrors(errors) {
  if (!errors) return [];
  if (Array.isArray(errors)) return errors;
  if (typeof errors === "object") {
    return Object.entries(errors).map(([field, reason]) => ({
      field,
      reason: Array.isArray(reason) ? reason[0] : reason,
    }));
  }
  return [];
}

function unwrapResponse(response) {
  const payload = response?.data;
  if (!payload || payload.success !== true) {
    throw {
      code: payload?.code || response?.status || 500,
      message: payload?.message || "Co loi xay ra. Vui long thu lai.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

function mapAxiosError(error) {
  const status = error?.response?.status || error?.code || 500;
  const payload = error?.response?.data;

  if (!error?.response && error?.code) {
    return error;
  }

  return {
    code: payload?.code || status,
    message: payload?.message || "Co loi xay ra. Vui long thu lai.",
    errors: normalizeErrors(payload?.errors),
  };
}

export async function fetchGlobalFeed({ page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/feed/global", {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchFollowingFeed({ page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/feed/following", {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
