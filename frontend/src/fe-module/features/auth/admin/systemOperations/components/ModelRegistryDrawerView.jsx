import { useState } from "react";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminFilterButton } from "../../components/ui";
import { MODEL_REGISTRY_VIEW_MODES } from "../constants/modelRegistryConstants.js";
import {
  deriveArtifactStatus,
  formatMetricNumber,
  getArtifactStatusBadgeClass,
  getArtifactStatusLabel,
} from "../utils/modelRegistryDisplayUtils.js";
import { ModelMetricsSections } from "./ModelMetricsSections.jsx";

function CopyPathButton({ path }) {
  const [copied, setCopied] = useState(false);

  if (!path) return <span className="text-admin-text-muted">—</span>;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(path);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <div className="flex items-start gap-2">
      <code className="break-all rounded bg-admin-surface-muted px-2 py-1 text-xs text-admin-text">
        {path}
      </code>
      <AdminFilterButton type="button" variant="ghost" className="shrink-0 min-h-9 px-2" onClick={handleCopy}>
        {copied ? "Đã copy" : "Copy"}
      </AdminFilterButton>
    </div>
  );
}

export function ModelRegistryDrawerView({
  open,
  artifact,
  viewMode,
  onClose,
  onViewChange,
}) {
  if (!open || !artifact) return null;

  const status = deriveArtifactStatus(artifact);
  const gate = artifact.metrics?.gate;
  const onnxVerify = artifact.metrics?.onnx_verify;

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  const isMetrics = viewMode === MODEL_REGISTRY_VIEW_MODES.METRICS;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <span
                className={`inline-flex rounded px-2 py-0.5 text-xs font-medium ${getArtifactStatusBadgeClass(status)}`}
              >
                {getArtifactStatusLabel(status)}
              </span>
              <span className="text-xs text-admin-text-muted">{artifact.format}</span>
            </div>
            <h2 className="font-mono text-xl font-semibold text-admin-text">v{artifact.version}</h2>
            <p className="mt-1 text-sm text-admin-text-secondary">{artifact.modelName}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-admin-text-secondary hover:bg-admin-surface"
            aria-label="Đóng drawer"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <div className="border-b border-admin-border px-4 py-3 sm:px-6">
          <div className="flex gap-2">
            <button
              type="button"
              className={tabClass(!isMetrics)}
              onClick={() => onViewChange?.(MODEL_REGISTRY_VIEW_MODES.DETAIL)}
            >
              Chi tiết
            </button>
            <button
              type="button"
              className={tabClass(isMetrics)}
              onClick={() => onViewChange?.(MODEL_REGISTRY_VIEW_MODES.METRICS)}
            >
              Metrics
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          {!isMetrics ? (
            <dl className="space-y-4 text-sm">
              <div>
                <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  Đường dẫn artifact
                </dt>
                <dd className="mt-2">
                  <CopyPathButton path={artifact.artifactPath} />
                </dd>
              </div>
              <div>
                <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  Huấn luyện lúc
                </dt>
                <dd className="mt-1 text-admin-text">{formatDateTime(artifact.trainedAt) || "—"}</dd>
              </div>
              <div>
                <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  Gate
                </dt>
                <dd className="mt-1 text-admin-text">
                  {gate?.passed === true
                    ? "Đạt ngưỡng"
                    : gate?.passed === false
                      ? "Không đạt ngưỡng"
                      : gate?.status || gate?.reason || "—"}
                </dd>
              </div>
              <div>
                <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  ONNX verify
                </dt>
                <dd className="mt-1 text-admin-text">
                  {onnxVerify?.passed === true
                    ? "Pass"
                    : onnxVerify?.passed === false
                      ? "Fail"
                      : typeof onnxVerify === "object"
                        ? JSON.stringify(onnxVerify)
                        : "—"}
                </dd>
              </div>
              <div>
                <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  AUC
                </dt>
                <dd className="mt-1 tabular-nums text-admin-text">
                  {formatMetricNumber(artifact.metrics?.auc)}
                </dd>
              </div>
            </dl>
          ) : (
            <ModelMetricsSections metrics={artifact.metrics} />
          )}
        </div>
      </aside>
    </div>
  );
}
