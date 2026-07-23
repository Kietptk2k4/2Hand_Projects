import {
  ARTIFACT_STATUS,
  ARTIFACT_STATUS_LABELS,
  RUNTIME_MODE_LABELS,
  RUNTIME_REASON_LABELS,
} from "../constants/modelRegistryConstants.js";

export function mapRecommendationModelArtifact(item) {
  if (!item) return null;

  return {
    version: item.version,
    format: item.format,
    artifactPath: item.artifactPath ?? item.artifact_path ?? "",
    isActive: Boolean(item.isActive ?? item.is_active),
    trainedAt: item.trainedAt ?? item.trained_at ?? null,
    modelName: item.modelName ?? item.model_name ?? "feed_ranker",
    metrics: item.metrics ?? null,
  };
}

export function mapRecommendationModelArtifacts(items) {
  if (!Array.isArray(items)) return [];
  return items.map(mapRecommendationModelArtifact).filter(Boolean);
}

export function deriveArtifactStatus(item) {
  if (item?.isActive) return ARTIFACT_STATUS.ACTIVE;

  const gate = item?.metrics?.gate;
  const rejected =
    gate?.status === "rejected_by_metrics" ||
    gate?.reason === "rejected_by_metrics" ||
    (gate?.passed === false && gate?.reason === "rejected_by_metrics");

  if (rejected) return ARTIFACT_STATUS.REJECTED;
  return ARTIFACT_STATUS.INACTIVE;
}

export function getArtifactStatusLabel(status) {
  return ARTIFACT_STATUS_LABELS[status] || status;
}

export function getArtifactStatusBadgeClass(status) {
  if (status === ARTIFACT_STATUS.ACTIVE) {
    return "bg-emerald-100 text-emerald-800";
  }
  if (status === ARTIFACT_STATUS.REJECTED) {
    return "bg-amber-100 text-amber-900";
  }
  return "bg-slate-100 text-slate-700";
}

export function computeModelRegistryStats(items) {
  const counts = {
    total: items.length,
    active: 0,
    rejected: 0,
    inactive: 0,
  };

  items.forEach((item) => {
    const status = deriveArtifactStatus(item);
    counts[status] += 1;
  });

  return {
    ...counts,
    presets: [
      { id: "active", label: ARTIFACT_STATUS_LABELS.active, count: counts.active },
      { id: "rejected", label: ARTIFACT_STATUS_LABELS.rejected, count: counts.rejected },
      { id: "inactive", label: ARTIFACT_STATUS_LABELS.inactive, count: counts.inactive },
    ],
  };
}

export function filterModelRegistryItems(items, statusFilter) {
  if (!statusFilter) return items;
  return items.filter((item) => deriveArtifactStatus(item) === statusFilter);
}

export function formatMetricNumber(value, digits = 4) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return "—";
  return Number(value).toFixed(digits);
}

export function formatRuntimeModeLabel(mode) {
  return RUNTIME_MODE_LABELS[mode] || mode || "—";
}

export function formatRuntimeReasonLabel(reason) {
  if (!reason) return null;
  return RUNTIME_REASON_LABELS[reason] || reason;
}

export function summarizeMetrics(item) {
  const metrics = item?.metrics;
  if (!metrics) return { auc: "—", precisionAt10: "—" };

  return {
    auc: formatMetricNumber(metrics.auc),
    precisionAt10: formatMetricNumber(metrics.precision_at_10 ?? metrics.precisionAt10),
  };
}
