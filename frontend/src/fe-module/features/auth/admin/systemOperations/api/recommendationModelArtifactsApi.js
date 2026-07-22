import { socialApiClient } from "../../../../../services/http/socialApiClient";

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

export async function fetchRecommendationModelArtifacts(modelName = "feed_ranker") {
  try {
    const response = await socialApiClient.get("/api/v1/social/admin/recommendation-model-artifacts", {
      params: { modelName },
    });
    return unwrapResponse(response) || [];
  } catch (error) {
    if (error?.code && error?.message) throw error;
    const status = error?.response?.status || 500;
    const payload = error?.response?.data;
    throw {
      code: payload?.code || status,
      message: payload?.message || "Co loi xay ra. Vui long thu lai.",
      errors: normalizeErrors(payload?.errors),
    };
  }
}
