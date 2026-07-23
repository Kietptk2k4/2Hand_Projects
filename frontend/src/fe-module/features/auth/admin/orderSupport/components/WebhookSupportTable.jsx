import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { WEBHOOK_PROCESSING_STATUS_LABELS } from "../constants/webhookSupportListConstants.js";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function ProviderBadge({ provider }) {
  return (
    <span className="rounded-md bg-admin-accent-soft px-2 py-0.5 text-xs font-bold text-admin-accent-strong">
      {provider}
    </span>
  );
}

export function WebhookSupportTable({
  logs,
  selectedLogId,
  onLogSelect,
  formatDateTime,
}) {
  if (!logs?.length) {
    return (
      <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
        <span
          className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
          aria-hidden="true"
        >
          webhook
        </span>
        <p className="mt-4 text-sm font-medium text-admin-text">Không có webhook log phù hợp</p>
        <p className="mx-auto mt-2 max-w-md text-sm text-admin-text-secondary">
          Thử điều chỉnh bộ lọc hoặc chọn preset nhanh ở trên.
        </p>
      </div>
    );
  }

  const rowClass = (logId) =>
    [
      "cursor-pointer transition-colors hover:bg-admin-surface-muted/70",
      selectedLogId === logId ? "bg-admin-accent-soft/20" : "",
    ].join(" ");

  return (
    <>
      <AdminMobileCardList>
        {logs.map((log) => (
          <AdminMobileCard key={log.log_id}>
            <button type="button" className="w-full text-left" onClick={() => onLogSelect?.(log)}>
              <div className="flex items-start justify-between gap-2">
                <ProviderBadge provider={log.provider} />
                <SupportStatusBadge
                  status={WEBHOOK_PROCESSING_STATUS_LABELS[log.processing_status] || log.processing_status}
                />
              </div>
              <p className="mt-2 font-mono text-xs text-admin-text-muted">{log.reference_id}</p>
              <p className="mt-1 text-sm text-admin-text">{log.event_type}</p>
              <p className="mt-2 text-xs text-admin-text-secondary">
                {formatDateTime(log.received_at)}
              </p>
            </button>
          </AdminMobileCard>
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="960px" ariaLabel="Nhật ký webhook hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Nhà cung cấp</AdminDataTableCell>
            <AdminDataTableCell header>Mã tham chiếu</AdminDataTableCell>
            <AdminDataTableCell header>Sự kiện</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Chữ ký</AdminDataTableCell>
            <AdminDataTableCell header>Nhận lúc</AdminDataTableCell>
            <AdminDataTableCell header>Idempotency</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {logs.map((log) => (
            <AdminDataTableRow
              key={log.log_id}
              className={rowClass(log.log_id)}
              onClick={() => onLogSelect?.(log)}
              onKeyDown={(event) => {
                if (event.key === "Enter") onLogSelect?.(log);
              }}
              tabIndex={0}
            >
              <AdminDataTableCell>
                <ProviderBadge provider={log.provider} />
              </AdminDataTableCell>
              <AdminDataTableCell className="font-mono text-xs">{log.reference_id}</AdminDataTableCell>
              <AdminDataTableCell>{log.event_type}</AdminDataTableCell>
              <AdminDataTableCell>
                <SupportStatusBadge
                  status={WEBHOOK_PROCESSING_STATUS_LABELS[log.processing_status] || log.processing_status}
                />
              </AdminDataTableCell>
              <AdminDataTableCell>
                {log.signature_valid == null
                  ? "—"
                  : log.signature_valid
                    ? "Hợp lệ"
                    : "Không hợp lệ"}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3 text-sm text-admin-text-secondary">
                {formatDateTime(log.received_at)}
              </AdminDataTableCell>
              <AdminDataTableCell className="max-w-[12rem] truncate font-mono text-xs text-admin-text-muted">
                {log.idempotency_key}
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
