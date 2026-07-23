import { formatVndPrice } from "../../../../social/utils/formatPrice";
import {
  COD_STAGE_META,
  formatSharePercent,
  getCodBucket,
} from "../utils/codPipelineHelpers.js";

export function CodPipelineStageStrip({
  pipeline,
  shares,
  isLoading,
  onStageClick,
}) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-3 lg:grid-cols-12">
        <div className="h-36 animate-pulse rounded-2xl bg-admin-surface-muted lg:col-span-5" />
        <div className="hidden items-center justify-center lg:col-span-1 lg:flex">
          <div className="h-6 w-6 rounded-full bg-admin-surface-muted" />
        </div>
        <div className="h-36 animate-pulse rounded-2xl bg-admin-surface-muted lg:col-span-3" />
        <div className="hidden items-center justify-center lg:col-span-1 lg:flex">
          <div className="h-6 w-6 rounded-full bg-admin-surface-muted" />
        </div>
        <div className="h-36 animate-pulse rounded-2xl bg-admin-surface-muted lg:col-span-2" />
      </div>
    );
  }

  const colSpans = ["lg:col-span-5", "lg:col-span-3", "lg:col-span-2"];

  return (
    <div className="grid grid-cols-1 gap-3 lg:grid-cols-12 lg:items-stretch">
      {COD_STAGE_META.map((stage, index) => {
        const bucket = getCodBucket(pipeline, stage.key);
        const share = shares?.[stage.key] ?? 0;
        const interactive = typeof onStageClick === "function";

        const card = (
          <>
            <div className="flex items-start justify-between gap-2">
              <div>
                <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
                  {stage.label}
                </p>
                <p className="mt-1 text-xs text-admin-text-secondary">{stage.hint}</p>
              </div>
              <span
                className={[
                  "material-symbols-outlined flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-xl",
                  stage.accentClass,
                  stage.surfaceClass,
                ].join(" ")}
                aria-hidden="true"
              >
                {stage.icon}
              </span>
            </div>

            <p className="mt-4 break-words text-2xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-[1.65rem]">
              {formatVndPrice(bucket.amount)}
            </p>

            <div className="mt-3 flex items-center justify-between gap-2 text-sm">
              <span className="tabular-nums text-admin-text-secondary">
                {bucket.itemCount} dòng đơn
              </span>
              <span
                className={[
                  "rounded-md px-1.5 py-0.5 text-xs font-semibold tabular-nums",
                  stage.surfaceClass,
                  stage.accentClass,
                ].join(" ")}
              >
                {formatSharePercent(share)}
              </span>
            </div>

            <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-admin-surface-muted">
              <div
                className={`h-full rounded-full ${stage.barClass}`}
                style={{ width: `${Math.min(100, Math.max(0, share))}%` }}
              />
            </div>

            {interactive ? (
              <p className="mt-3 text-xs font-medium text-admin-accent">
                Xem đơn liên quan →
              </p>
            ) : null}
          </>
        );

        const wrapperClass = [
          "min-w-0 rounded-2xl border border-admin-border bg-admin-surface-raised p-4 text-left transition-colors",
          colSpans[index],
          interactive
            ? "hover:border-admin-accent-border hover:bg-admin-accent-soft/15 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
            : "",
        ]
          .filter(Boolean)
          .join(" ");

        return (
          <div key={stage.key} className="contents">
            {interactive ? (
              <button
                type="button"
                onClick={() => onStageClick(stage.key)}
                className={wrapperClass}
              >
                {card}
              </button>
            ) : (
              <div className={wrapperClass}>{card}</div>
            )}

            {index < COD_STAGE_META.length - 1 ? (
              <div
                className="hidden items-center justify-center text-admin-text-muted lg:col-span-1 lg:flex"
                aria-hidden="true"
              >
                <span className="material-symbols-outlined text-2xl">arrow_forward</span>
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
