import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
} from "../../components/ui";
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

      <AdminDataTable minWidth="960px" ariaLabel="Nhật ký hành động admin">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Admin</AdminDataTableCell>
            <AdminDataTableCell header>Action</AdminDataTableCell>
            <AdminDataTableCell header>Target</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>IP</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {logs.map((log) => {
            const isSelected = selectedLogId === log.logId;
            return (
              <AdminDataTableRow
                key={log.logId}
                isSelected={isSelected}
                onClick={() => onSelectLog?.(log.logId)}
                className="cursor-pointer"
              >
                <AdminDataTableCell className="whitespace-nowrap">
                  {formatDateTime(log.createdAt)}
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                  {truncateId(log.adminId)}
                </AdminDataTableCell>
                <AdminDataTableCell className="font-medium">{log.actionType}</AdminDataTableCell>
                <AdminDataTableCell>
                  <div className="text-xs text-admin-text-muted">{log.targetType || "—"}</div>
                  <div className="font-mono text-xs text-admin-text">
                    {truncateId(log.targetId, 16)}
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AuditStatusBadge status={log.status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                  {log.ipAddress || "—"}
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
