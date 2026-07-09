import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { AdminFilterButton, AdminStatusBadge, AdminSurfaceCard } from "../../../components/ui";
import { getSessionStatusVariant } from "../ui/investigationStatusVariants.js";

export function InvestigationSessionCard({ session, statusLabel, canRevokeSession, onRevokeSession }) {
  const status = session.status || "ACTIVE";
  const showRevoke = canRevokeSession && status === "ACTIVE";

  return (
    <li className="rounded-xl border border-admin-border bg-admin-surface p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-medium text-admin-text">
            {session.device_id || "Thiết bị không xác định"}
          </p>
          <p className="mt-1 break-all text-xs text-admin-text-muted">
            Mã phiên: {session.session_id || "—"}
          </p>
        </div>
        <AdminStatusBadge variant={getSessionStatusVariant(status)}>{statusLabel}</AdminStatusBadge>
      </div>
      <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-admin-text-muted">IP</dt>
          <dd className="text-admin-text">{session.ip_address || "—"}</dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Tạo lúc</dt>
          <dd className="text-admin-text">{formatDateTime(session.created_at)}</dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Cập nhật</dt>
          <dd className="text-admin-text">{formatDateTime(session.updated_at)}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-admin-text-muted">Trình duyệt / ứng dụng</dt>
          <dd className="break-all text-admin-text">{session.user_agent || "—"}</dd>
        </div>
      </dl>
      {showRevoke ? (
        <div className="mt-4 flex justify-end border-t border-admin-border-subtle pt-3">
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 w-full border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft sm:w-auto"
            onClick={() => onRevokeSession?.(session)}
          >
            Thu hồi phiên admin
          </AdminFilterButton>
        </div>
      ) : null}
    </li>
  );
}

export function InvestigationSessionsListView({
  sessions,
  getStatusLabel,
  canRevokeSession,
  hasNext,
  loadMoreStatus,
  onRevokeSession,
  onLoadMore,
}) {
  if (sessions.length === 0) {
    return (
      <AdminSurfaceCard padding="lg">
        <p className="text-sm text-admin-text-muted">Không có phiên đăng nhập phù hợp bộ lọc.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="none">
      <div className="border-b border-admin-border px-4 py-3 sm:px-6">
        <p className="text-sm font-medium text-admin-text">{sessions.length} phiên hiển thị</p>
      </div>
      <ul className="flex flex-col gap-3 p-2 sm:p-4">
        {sessions.map((session) => (
          <InvestigationSessionCard
            key={session.session_id}
            session={session}
            statusLabel={getStatusLabel(session.status || "ACTIVE")}
            canRevokeSession={canRevokeSession}
            onRevokeSession={onRevokeSession}
          />
        ))}
      </ul>

      {hasNext ? (
        <div className="flex justify-center border-t border-admin-border px-4 py-6">
          <AdminFilterButton
            type="button"
            variant="primary"
            className="min-w-[10rem]"
            disabled={loadMoreStatus === "loading"}
            onClick={onLoadMore}
          >
            {loadMoreStatus === "loading" ? "Đang tải…" : "Tải thêm"}
          </AdminFilterButton>
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}
