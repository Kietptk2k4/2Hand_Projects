import { getAuditTargetTypeLabel } from "../constants/adminAuditActionLabels.js";
import { formatAuditLogDateTime } from "../utils/auditDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
} from "../../components/ui";
import { AuditActionCell } from "./AuditActionTypeBadge.jsx";
import { AuditLogCardList } from "./AuditLogCardList.jsx";
import { AuditStatusBadge } from "./AuditStatusBadge.jsx";

function truncateId(value, length = 12) {
  if (!value) return "—";
  if (value.length <= length) return value;
  return `${value.slice(0, length)}…`;
}

export function AdminActionLogsTable({ logs, selectedLogId, onSelectLog }) {
  if (!logs?.length) return null;

  return (
    <>
      <AuditLogCardList logs={logs} selectedLogId={selectedLogId} onSelectLog={onSelectLog} />

      <AdminDataTable minWidth="880px" ariaLabel="Nhật ký hành động admin">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Admin</AdminDataTableCell>
            <AdminDataTableCell header>Hành động</AdminDataTableCell>
            <AdminDataTableCell header>Đối tượng</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {logs.map((log) => {
            const isSelected = selectedLogId === log.logId;
            const { time, date } = formatAuditLogDateTime(log.createdAt);
            return (
              <AdminDataTableRow
                key={log.logId}
                isSelected={isSelected}
                onClick={() => onSelectLog?.(log.logId)}
                className="cursor-pointer"
              >
                <AdminDataTableCell className="whitespace-nowrap">
                  <div className="tabular-nums text-sm font-medium text-admin-text">{time}</div>
                  <div className="tabular-nums text-xs text-admin-text-muted">{date}</div>
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                  {truncateId(log.adminId)}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AuditActionCell actionType={log.actionType} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <div className="text-sm text-admin-text">
                    {getAuditTargetTypeLabel(log.targetType)}
                  </div>
                  <div className="font-mono text-xs text-admin-text-muted">
                    {truncateId(log.targetId, 16)}
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AuditStatusBadge status={log.status} />
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
