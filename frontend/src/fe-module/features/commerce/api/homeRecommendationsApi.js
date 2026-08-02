import { commerceApiClient } from "../../../services/http/commerceApiClient";

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
      message: payload?.message || "Có lỗi xảy ra. Vui lòng thử lại.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

export async function fetchHomeRecommendations() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/home/recommendations");
    return unwrapResponse(response);
  } catch (error) {
    if (error?.code && error?.message) throw error;
    const status = error?.response?.status || 500;
    const payload = error?.response?.data;
    throw {
      code: payload?.code || status,
      message: payload?.message || "Không tải được đề xuất Home. Vui lòng thử lại.",
      errors: normalizeErrors(payload?.errors),
    };
  }
}
