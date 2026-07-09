import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminFilterButton, AdminSurfaceCard } from "../../components/ui";

export function SocialModerationPanelView({
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
    <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
      <div>
        <h3 className="text-base font-semibold text-admin-text">Mục tiêu kiểm duyệt</h3>
        <p className="mt-1 text-sm text-admin-text-secondary">
          Admin Service ghi log và publish event. Social Service áp dụng trạng thái cuối cùng.
        </p>
        <p className="mt-3 break-all font-mono text-sm text-admin-text">{targetId}</p>
      </div>

      <div className="mt-5 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
        {canModerate ? (
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={disabled}
            onClick={onModerate}
            className="inline-flex gap-2 sm:min-w-[11rem]"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              gavel
            </span>
            Kiểm duyệt ({targetLabel})
          </AdminFilterButton>
        ) : null}
        {canRestore ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            disabled={disabled}
            onClick={onRestore}
            className="inline-flex gap-2 sm:min-w-[11rem]"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              restore
            </span>
            Khôi phục
          </AdminFilterButton>
        ) : null}
      </div>

      {lastResult ? (
        <div className="mt-5 rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-4 text-sm">
          <p className="font-medium text-admin-text">Lần thao tác gần nhất</p>
          <dl className="mt-2 grid gap-1 text-admin-text-secondary">
            {lastResult.action ? (
              <div>
                <dt className="inline font-medium text-admin-text">Hành động: </dt>
                <dd className="inline">{lastResult.action}</dd>
              </div>
            ) : null}
            <div>
              <dt className="inline font-medium text-admin-text">moderation_log_id: </dt>
              <dd className="inline break-all font-mono">{lastResult.moderationLogId || "—"}</dd>
            </div>
            {lastResult.actedAt ? (
              <div>
                <dt className="inline font-medium text-admin-text">Thời gian: </dt>
                <dd className="inline">{formatDateTime(lastResult.actedAt)}</dd>
              </div>
            ) : null}
          </dl>
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}
