import { useEffect } from "react";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminStatusBadge } from "../../components/ui";
import { useAuditAdminSummaries } from "../../adminAudit/hooks/useAuditAdminSummaries.js";
import { getCommentModerationActionLabel } from "../constants/commentModerationDisplayLabels.js";
import { useContentModerationPermissions } from "../hooks/useContentModerationPermissions.js";
import { useCommentModerationHistory } from "../hooks/useCommentModerationHistory.js";

function historyBadgeVariant(action) {
  const normalized = String(action || "").toUpperCase();
  if (normalized === "RESTORE") return "success";
  if (normalized === "HIDE") return "warning";
  if (normalized === "REMOVE") return "danger";
  return "neutral";
}

export function CommentModerationHistoryPanel({ commentId, refreshToken }) {
  const { canReadCommentHistory } = useContentModerationPermissions();
  const { result, status, errorMessage, refetch } = useCommentModerationHistory(
    canReadCommentHistory ? commentId : null,
  );

  const adminIds = (result?.history || []).map((entry) => entry.adminId);
  const adminSummaries = useAuditAdminSummaries(adminIds);

  useEffect(() => {
    if (!refreshToken || !canReadCommentHistory) return;
    refetch();
  }, [canReadCommentHistory, refreshToken, refetch]);

  if (!canReadCommentHistory || !commentId) return null;

  return (
    <section className="mt-8 border-t border-admin-border-subtle pt-6">
      <div className="flex items-center justify-between gap-3">
        <h3 className="text-sm font-semibold text-admin-text">Lịch sử kiểm duyệt</h3>
        {result?.totalElements ? (
          <span className="text-xs text-admin-text-muted">{result.totalElements} bản ghi</span>
        ) : null}
      </div>

      {status === "loading" ? (
        <div className="mt-4 space-y-2">
          {[1, 2, 3].map((key) => (
            <div key={key} className="h-12 animate-pulse rounded-lg bg-admin-surface-muted" />
          ))}
        </div>
      ) : null}

      {status === "error" ? (
        <div className="mt-4 rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 p-4">
          <p className="text-sm text-admin-danger">{errorMessage}</p>
          <button
            type="button"
            onClick={refetch}
            className="mt-3 inline-flex min-h-9 items-center rounded-lg border border-admin-border px-3 text-xs font-medium text-admin-text-secondary hover:bg-admin-surface-muted"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {status === "ready" && result?.history?.length ? (
        <ol className="mt-4 space-y-3">
          {result.history.map((entry) => {
            const admin = adminSummaries[entry.adminId];
            const adminLabel = admin?.displayName || admin?.email || entry.adminId || "—";

            return (
              <li
                key={entry.moderationLogId}
                className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3"
              >
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <AdminStatusBadge variant={historyBadgeVariant(entry.action)}>
                    {getCommentModerationActionLabel(entry.action)}
                  </AdminStatusBadge>
                  <time className="text-xs tabular-nums text-admin-text-muted">
                    {formatDateTime(entry.createdAt)}
                  </time>
                </div>
                <p className="mt-2 text-sm text-admin-text">{entry.reason}</p>
                {entry.note ? (
                  <p className="mt-1 text-xs text-admin-text-secondary">{entry.note}</p>
                ) : null}
                <p className="mt-2 text-xs text-admin-text-secondary">
                  Admin: <span className="font-medium text-admin-text">{adminLabel}</span>
                </p>
              </li>
            );
          })}
        </ol>
      ) : null}

      {status === "ready" && !result?.history?.length ? (
        <p className="mt-4 text-sm text-admin-text-secondary">Chưa có lịch sử kiểm duyệt.</p>
      ) : null}
    </section>
  );
}
