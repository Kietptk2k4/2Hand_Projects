import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../../components/ui";
import { getActorTypeLabel } from "../../utils/investigationLabels.js";
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "../EnforcementBadges.jsx";

function EnforcementLogTimeline({ logs = [] }) {
  if (logs.length === 0) {
    return <p className="text-sm text-admin-text-muted">Không có log chuyển trạng thái.</p>;
  }

  return (
    <div className="relative ml-2 max-h-64 space-y-4 overflow-y-auto border-l-2 border-admin-border pl-4">
      {logs.map((log) => (
        <div key={log.log_id || `${log.created_at}-${log.new_status}`} className="relative">
          <span className="absolute -left-[21px] top-1.5 h-3 w-3 rounded-full border-2 border-admin-accent bg-admin-surface" />
          <p className="text-sm font-medium text-admin-text">
            {log.old_status ? `${log.old_status} → ${log.new_status}` : log.new_status}
          </p>
          <p className="mt-1 text-xs text-admin-text-muted">
            {formatDateTime(log.created_at)} · {getActorTypeLabel(log.actor_type)}
            {log.admin_id ? ` · ${log.admin_id.slice(0, 8)}…` : ""}
          </p>
          {log.note ? <p className="mt-1 text-sm text-admin-text-secondary">{log.note}</p> : null}
        </div>
      ))}
    </div>
  );
}

function HistoryMobileCard({ item, expanded, onToggle }) {
  return (
    <div className="overflow-hidden rounded-xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
      <AdminMobileCard
        onClick={onToggle}
        ariaLabel={`${expanded ? "Thu gọn" : "Mở rộng"} lịch sử ${item.enforcement_id?.slice(0, 8)}`}
        isSelected={expanded}
        className="rounded-none border-0 shadow-none"
      >
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0 flex-1">
            <p className="font-mono text-xs text-admin-text">{item.enforcement_id?.slice(0, 12)}…</p>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <EnforcementActionBadge actionType={item.action_type} />
              <EnforcementStatusBadge status={item.status} />
            </div>
            <p className="mt-2 text-sm font-medium text-admin-text">{item.reason_code}</p>
            <p className="mt-1 text-xs text-admin-text-muted">
              {formatDateTime(item.updated_at || item.created_at)}
            </p>
          </div>
          <span
            className="inline-flex min-h-11 min-w-11 shrink-0 items-center justify-center text-admin-text-muted transition-transform"
            style={{ transform: expanded ? "rotate(180deg)" : "" }}
            aria-hidden
          >
            ▼
          </span>
        </div>
      </AdminMobileCard>
      {expanded ? (
        <div className="border-t border-admin-border bg-admin-surface-muted px-4 py-4">
          <h4 className="mb-3 text-sm font-semibold text-admin-text">Dòng thời gian audit</h4>
          <EnforcementLogTimeline logs={item.logs || []} />
        </div>
      ) : null}
    </div>
  );
}

function HistoryRow({ item, expanded, onToggle }) {
  return (
    <>
      <AdminDataTableRow
        onClick={onToggle}
        className="cursor-pointer"
        aria-expanded={expanded}
      >
        <AdminDataTableCell className="py-3 text-center text-admin-text-muted">
          <span
            className="inline-block min-h-11 min-w-11 leading-[2.75rem] transition-transform"
            style={{ transform: expanded ? "rotate(180deg)" : "" }}
            aria-hidden
          >
            ▼
          </span>
        </AdminDataTableCell>
        <AdminDataTableCell>
          <div className="font-mono text-xs text-admin-text">{item.enforcement_id?.slice(0, 12)}…</div>
          <div className="mt-1 text-xs text-admin-text-muted">
            Admin: {item.enforced_by?.slice(0, 8) || "—"}…
          </div>
        </AdminDataTableCell>
        <AdminDataTableCell>
          <EnforcementActionBadge actionType={item.action_type} />
        </AdminDataTableCell>
        <AdminDataTableCell className="max-w-[200px]">
          <div className="truncate text-sm font-medium text-admin-text">{item.reason_code}</div>
          <div className="mt-1 truncate text-xs text-admin-text-muted">{item.description}</div>
        </AdminDataTableCell>
        <AdminDataTableCell>
          <EnforcementStatusBadge status={item.status} />
        </AdminDataTableCell>
        <AdminDataTableCell className="text-sm text-admin-text">
          {formatDateTime(item.updated_at || item.created_at)}
        </AdminDataTableCell>
      </AdminDataTableRow>
      {expanded ? (
        <tr className="bg-admin-surface-muted">
          <td colSpan={6} className="px-4 py-4 sm:px-6">
            <h4 className="mb-3 text-sm font-semibold text-admin-text">Dòng thời gian audit</h4>
            <EnforcementLogTimeline logs={item.logs || []} />
          </td>
        </tr>
      ) : null}
    </>
  );
}

export function InvestigationEnforcementHistoryTable({
  items,
  expandedId,
  onToggle,
  hasMore,
  loadMoreStatus,
  onLoadMore,
}) {
  if (items.length === 0) {
    return (
      <AdminSurfaceCard padding="lg">
        <p className="text-sm text-admin-text-muted">Chưa có lịch sử enforcement.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="border-b border-admin-border px-4 py-3 sm:px-6">
        <p className="text-sm font-medium text-admin-text">{items.length} bản ghi lịch sử</p>
      </div>

      <AdminMobileCardList className="p-4">
        {items.map((item) => (
          <HistoryMobileCard
            key={item.enforcement_id}
            item={item}
            expanded={expandedId === item.enforcement_id}
            onToggle={() => onToggle(item.enforcement_id)}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Lịch sử enforcement">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header className="w-12" />
            <AdminDataTableCell header>ID thực thi</AdminDataTableCell>
            <AdminDataTableCell header>Loại</AdminDataTableCell>
            <AdminDataTableCell header>Lý do</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Cập nhật lúc</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <HistoryRow
              key={item.enforcement_id}
              item={item}
              expanded={expandedId === item.enforcement_id}
              onToggle={() => onToggle(item.enforcement_id)}
            />
          ))}
        </AdminDataTableBody>
      </AdminDataTable>

      {hasMore ? (
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
