import { formatDateTime } from "../../../security/utils/formatDateTime.js";

export function SocialModerationPanel({
  targetLabel,
  targetId,
  canModerate,
  canRestore,
  lastResult,
  disabled,
  onModerate,
  onRestore,
}) {
  return (
    <div className="space-y-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div>
        <h3 className="text-headline-sm font-semibold text-on-surface">Muc tieu kiem duyet</h3>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          Admin Service ghi log va publish event. Social Service ap dung trang thai cuoi cung.
        </p>
        <p className="mt-2 break-all font-mono text-sm text-on-surface">{targetId}</p>
      </div>

      <div className="flex flex-wrap gap-3">
        {canModerate ? (
          <button
            type="button"
            disabled={disabled}
            onClick={onModerate}
            className="inline-flex items-center gap-1 rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-primary/5 disabled:opacity-50"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">gavel</span>
            Kiem duyet ({targetLabel})
          </button>
        ) : null}
        {canRestore ? (
          <button
            type="button"
            disabled={disabled}
            onClick={onRestore}
            className="inline-flex items-center gap-1 rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface-variant hover:border-primary hover:text-primary disabled:opacity-50"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">restore</span>
            Khoi phuc
          </button>
        ) : null}
      </div>

      {lastResult ? (
        <div className="rounded-lg border border-outline-variant bg-surface-container-low/50 p-4 text-sm">
          <p className="font-medium text-on-surface">Lan thao tac gan nhat</p>
          <dl className="mt-2 grid gap-1 text-on-surface-variant">
            {lastResult.action ? (
              <div>
                <dt className="inline font-medium">action: </dt>
                <dd className="inline">{lastResult.action}</dd>
              </div>
            ) : null}
            <div>
              <dt className="inline font-medium">moderation_log_id: </dt>
              <dd className="inline font-mono break-all">{lastResult.moderationLogId || "—"}</dd>
            </div>
            {lastResult.actedAt ? (
              <div>
                <dt className="inline font-medium">thoi gian: </dt>
                <dd className="inline">{formatDateTime(lastResult.actedAt)}</dd>
              </div>
            ) : null}
          </dl>
        </div>
      ) : null}
    </div>
  );
}