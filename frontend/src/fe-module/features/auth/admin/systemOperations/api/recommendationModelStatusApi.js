import { socialApiClient } from "../../../../../services/http/socialApiClient";
import { commerceApiClient } from "../../../../../services/http/commerceApiClient";
import { MODEL_REGISTRY_TARGETS } from "../constants/modelRegistryConstants.js";

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

function resolveTarget(modelName) {
  return (
    MODEL_REGISTRY_TARGETS.find((t) => t.modelName === modelName) || MODEL_REGISTRY_TARGETS[0]
  );
}

export async function fetchRecommendationModelStatus(modelName = "feed_ranker") {
  const target = resolveTarget(modelName);
  try {
    if (target.source === "commerce") {
      const response = await commerceApiClient.get(
        "/commerce/api/v1/admin/home/recommendation-model-status",
      );
      return unwrapResponse(response);
    }
    const response = await socialApiClient.get("/api/v1/social/admin/recommendation-model-status");
    return unwrapResponse(response);
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
