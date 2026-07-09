import { Fragment } from "react";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function PayloadSummary({ summary }) {
  if (!summary || typeof summary !== "object") {
    return <span className="text-admin-text-muted">—</span>;
  }
  return (
    <pre className="max-w-full overflow-x-auto rounded-lg bg-admin-surface-muted p-3 text-xs text-admin-text">
      {JSON.stringify(summary, null, 2)}
    </pre>
  );
}

export function WebhookLogsTable({
  logs,
  expandedLogId,
  onToggleExpand,
  formatDateTime,
}) {
  if (!logs?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có webhook log phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {logs.map((log) => {
          const isExpanded = expandedLogId === log.log_id;
          return (
            <AdminMobileCard key={log.log_id}>
              <div className="flex items-start justify-between gap-2">
                <span className="rounded-md bg-admin-accent-soft px-2 py-0.5 text-xs font-bold text-admin-accent-strong">
                  {log.provider}
                </span>
                <SupportStatusBadge status={log.processing_status} />
              </div>
              <p className="mt-2 font-mono text-xs text-admin-text-muted">{log.reference_id}</p>
              <p className="mt-1 text-sm text-admin-text">{log.event_type}</p>
              <p className="mt-2 text-xs text-admin-text-secondary">
                {formatDateTime(log.received_at)} · Retry {log.retry_count ?? 0}
              </p>
              <AdminFilterButton
                type="button"
                variant="secondary"
                className="mt-3 w-full"
                onClick={() => onToggleExpand(log.log_id)}
              >
                {isExpanded ? "Thu gọn" : "Xem payload"}
              </AdminFilterButton>
              {isExpanded ? (
                <div className="mt-3">
                  <PayloadSummary summary={log.payload_summary} />
                </div>
              ) : null}
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Nhật ký webhook hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Provider</AdminDataTableCell>
            <AdminDataTableCell header>Reference</AdminDataTableCell>
            <AdminDataTableCell header>Event</AdminDataTableCell>
            <AdminDataTableCell header>Status</AdminDataTableCell>
            <AdminDataTableCell header>Chữ ký</AdminDataTableCell>
            <AdminDataTableCell header>Retry</AdminDataTableCell>
            <AdminDataTableCell header>Nhận lúc</AdminDataTableCell>
            <AdminDataTableCell header>Payload</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {logs.map((log) => (
            <Fragment key={log.log_id}>
              <AdminDataTableRow>
                <AdminDataTableCell>
                  <span className="rounded-md bg-admin-accent-soft px-2 py-0.5 text-xs font-bold text-admin-accent-strong">
                    {log.provider}
                  </span>
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{log.reference_id}</AdminDataTableCell>
                <AdminDataTableCell>{log.event_type}</AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={log.processing_status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  {log.signature_valid == null
                    ? "—"
                    : log.signature_valid
                      ? "Hợp lệ"
                      : "Không hợp lệ"}
                </AdminDataTableCell>
                <AdminDataTableCell>{log.retry_count ?? 0}</AdminDataTableCell>
                <AdminDataTableCell className="py-3 text-sm text-admin-text-secondary">
                  {formatDateTime(log.received_at)}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <button
                    type="button"
                    onClick={() => onToggleExpand(log.log_id)}
                    className="min-h-11 py-2 text-xs font-medium text-admin-accent hover:underline"
                  >
                    {expandedLogId === log.log_id ? "Thu gọn" : "Xem"}
                  </button>
                </AdminDataTableCell>
              </AdminDataTableRow>
              {expandedLogId === log.log_id ? (
                <tr>
                  <td colSpan={8} className="bg-admin-surface-muted/50 px-4 py-3">
                    <PayloadSummary summary={log.payload_summary} />
                  </td>
                </tr>
              ) : null}
            </Fragment>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
