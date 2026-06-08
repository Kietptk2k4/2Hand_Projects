import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AuditStatusBadge } from "./AuditStatusBadge.jsx";

function truncateId(value, length = 12) {
  if (!value) return "—";
  if (value.length <= length) return value;
  return `${value.slice(0, length)}…`;
}

export function AdminActionLogsTable({ logs, selectedLogId, onSelectLog }) {
  if (!logs?.length) return null;

  return (
    <div className="overflow-x-auto">
      <table className="min-w-[960px] w-full text-left text-sm">
        <thead>
          <tr className="border-b border-outline-variant text-on-surface-variant">
            <th className="py-3 pr-4 font-medium">Thoi gian</th>
            <th className="py-3 pr-4 font-medium">Admin</th>
            <th className="py-3 pr-4 font-medium">Action</th>
            <th className="py-3 pr-4 font-medium">Target</th>
            <th className="py-3 pr-4 font-medium">Status</th>
            <th className="py-3 font-medium">IP</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => {
            const isSelected = selectedLogId === log.logId;
            return (
              <tr
                key={log.logId}
                onClick={() => onSelectLog?.(log.logId)}
                className={[
                  "cursor-pointer border-b border-outline-variant/60 transition-colors",
                  isSelected ? "bg-primary/5" : "hover:bg-surface-container-low/70",
                ].join(" ")}
              >
                <td className="py-3 pr-4 whitespace-nowrap text-on-surface">
                  {formatDateTime(log.createdAt)}
                </td>
                <td className="py-3 pr-4 font-mono text-xs text-on-surface-variant">
                  {truncateId(log.adminId)}
                </td>
                <td className="py-3 pr-4 font-medium text-on-surface">{log.actionType}</td>
                <td className="py-3 pr-4">
                  <div className="text-xs text-on-surface-variant">{log.targetType || "—"}</div>
                  <div className="font-mono text-xs text-on-surface">{truncateId(log.targetId, 16)}</div>
                </td>
                <td className="py-3 pr-4">
                  <AuditStatusBadge status={log.status} />
                </td>
                <td className="py-3 font-mono text-xs text-on-surface-variant">{log.ipAddress || "—"}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}