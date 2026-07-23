import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import { apiError, apiSuccess } from "../utils/response";

const MOCK_MODEL_ARTIFACTS = [
  {
    version: 3,
    format: "ONNX",
    artifactPath: "artifacts/feed_ranker/v3/model.onnx",
    modelName: "feed_ranker",
    isActive: true,
    trainedAt: "2026-07-22T08:30:00Z",
    metrics: {
      auc: 0.81,
      precision_at_10: 0.42,
      gate: { passed: true, status: "passed", reason: null, failed: [] },
      onnx_verify: { passed: true, detail: "shape ok" },
      baseline: { auc: 0.78, precision_at_10: 0.39 },
      delta: { auc: 0.03, precision_at_10: 0.03 },
      feature_order: ["user_age_days", "post_likes"],
    },
  },
  {
    version: 2,
    format: "ONNX",
    artifactPath: "artifacts/feed_ranker/v2/model.onnx",
    modelName: "feed_ranker",
    isActive: false,
    trainedAt: "2026-07-22T07:30:00Z",
    metrics: {
      auc: 0.73,
      precision_at_10: 0.31,
      gate: { passed: false, status: "rejected_by_metrics", reason: "rejected_by_metrics", failed: ["precision_at_10"] },
      onnx_verify: { passed: true },
    },
  },
  {
    version: 1,
    format: "ONNX",
    artifactPath: "artifacts/feed_ranker/v1/model.onnx",
    modelName: "feed_ranker",
    isActive: false,
    trainedAt: "2026-07-21T12:00:00Z",
    metrics: {
      auc: 0.7,
      precision_at_10: 0.35,
      gate: { passed: true, status: "passed" },
    },
  },
];

const MOCK_RUNTIME_STATUS = {
  mode: "lightgbm",
  modelVersion: 3,
  modelName: "feed_ranker",
  reason: null,
  configuredRankingModel: "lightgbm",
};

function getActor(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  return mockUsers.find((item) => token.includes(item.id)) || null;
}

function isAuthorized(request) {
  const actor = getActor(request);
  return Boolean(actor?.is_admin);
}

export const adminModelRegistryHandlers = [
  http.get("*/api/v1/social/admin/recommendation-model-artifacts", async ({ request }) => {
    await delay(200);
    if (!isAuthorized(request)) {
      return HttpResponse.json(apiError("FORBIDDEN", "Access denied"), { status: 403 });
    }

    const url = new URL(request.url);
    const modelName = url.searchParams.get("modelName") || "feed_ranker";
    const items = MOCK_MODEL_ARTIFACTS.filter((item) => item.modelName === modelName);
    return HttpResponse.json(apiSuccess(200, "Lay danh sach model artifact thanh cong.", items));
  }),

  http.get("*/api/v1/social/admin/recommendation-model-status", async ({ request }) => {
    await delay(150);
    if (!isAuthorized(request)) {
      return HttpResponse.json(apiError("FORBIDDEN", "Access denied"), { status: 403 });
    }
    return HttpResponse.json(
      apiSuccess(200, "Lay trang thai ranking model thanh cong.", MOCK_RUNTIME_STATUS),
    );
  }),
];
