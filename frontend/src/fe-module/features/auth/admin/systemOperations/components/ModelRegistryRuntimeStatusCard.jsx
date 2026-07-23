import {
  formatRuntimeModeLabel,
  formatRuntimeReasonLabel,
} from "../utils/modelRegistryDisplayUtils.js";

function StatusSkeleton() {
  return <div className="h-24 animate-pulse rounded-xl border border-admin-border bg-admin-surface-muted" />;
}

export function ModelRegistryRuntimeStatusCard({ runtimeStatus, status, errorMessage, onRetry }) {
  if (status === "loading") return <StatusSkeleton />;

  if (status === "error") {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
        <p>{errorMessage || "Không tải được trạng thái runtime."}</p>
        {onRetry ? (
          <button type="button" className="mt-2 underline" onClick={onRetry}>
            Thử lại
          </button>
        ) : null}
      </div>
    );
  }

  if (!runtimeStatus) return null;

  const isLightgbm = runtimeStatus.mode === "lightgbm";
  const reasonLabel = formatRuntimeReasonLabel(runtimeStatus.reason);

  return (
    <div
      className={`rounded-xl border px-4 py-4 ${
        isLightgbm
          ? "border-emerald-200 bg-emerald-50/60"
          : "border-amber-200 bg-amber-50/60"
      }`}
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
            Trạng thái phục vụ feed
          </p>
          <p className="mt-2 text-lg font-semibold text-admin-text">
            {isLightgbm
              ? `${formatRuntimeModeLabel(runtimeStatus.mode)} v${runtimeStatus.modelVersion ?? "—"}`
              : formatRuntimeModeLabel(runtimeStatus.mode)}
          </p>
          {reasonLabel ? (
            <p className="mt-1 text-sm text-amber-900">{reasonLabel}</p>
          ) : null}
          {runtimeStatus.modelName ? (
            <p className="mt-2 text-xs text-admin-text-secondary">
              Model: <span className="font-mono">{runtimeStatus.modelName}</span>
            </p>
          ) : null}
        </div>
        <div className="text-sm text-admin-text-secondary">
          <p>
            Cấu hình mong muốn:{" "}
            <span className="font-medium text-admin-text">
              {runtimeStatus.configuredRankingModel || "—"}
            </span>
          </p>
        </div>
      </div>
    </div>
  );
}
