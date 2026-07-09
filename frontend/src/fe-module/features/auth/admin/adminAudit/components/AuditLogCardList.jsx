import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminMobileCard, AdminMobileCardList } from "../../components/ui";
import { AuditStatusBadge } from "./AuditStatusBadge.jsx";

function truncateId(value, length = 12) {
  if (!value) return "—";
  if (value.length <= length) return value;
  return `${value.slice(0, length)}…`;
}

export function AuditLogCardList({ logs, selectedLogId, onSelectLog }) {
  if (!logs?.length) return null;

  return (
    <AdminMobileCardList>
      {logs.map((log) => {
        const isSelected = selectedLogId === log.logId;
        return (
          <AdminMobileCard
            key={log.logId}
            isSelected={isSelected}
            onClick={() => onSelectLog?.(log.logId)}
            ariaLabel={`Xem nhật ký ${log.actionType} lúc ${formatDateTime(log.createdAt)}`}
          >
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <p className="font-medium text-admin-text">{log.actionType}</p>
                <p className="mt-0.5 text-sm text-admin-text-secondary">
                  {formatDateTime(log.createdAt)}
                </p>
              </div>
              <AuditStatusBadge status={log.status} />
            </div>
            <dl className="mt-3 grid gap-2 text-sm">
              <div>
                <dt className="text-admin-text-muted">Admin</dt>
                <dd className="font-mono text-xs text-admin-text">{truncateId(log.adminId)}</dd>
              </div>
              <div>
                <dt className="text-admin-text-muted">Target</dt>
                <dd className="text-admin-text">
                  <span className="text-xs text-admin-text-muted">{log.targetType || "—"}</span>
                  <span className="mt-0.5 block font-mono text-xs">{truncateId(log.targetId, 16)}</span>
                </dd>
              </div>
              <div>
                <dt className="text-admin-text-muted">IP</dt>
                <dd className="font-mono text-xs text-admin-text">{log.ipAddress || "—"}</dd>
              </div>
            </dl>
          </AdminMobileCard>
        );
      })}
    </AdminMobileCardList>
  );
}
