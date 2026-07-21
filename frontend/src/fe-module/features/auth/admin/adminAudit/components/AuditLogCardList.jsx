import { getAuditActionLabel, getAuditTargetTypeLabel } from "../constants/adminAuditActionLabels.js";
import { formatAuditLogDateTime } from "../utils/auditDateTimeDisplay.js";
import { AdminMobileCard, AdminMobileCardList } from "../../components/ui";
import { AuditActionTypeBadge } from "./AuditActionTypeBadge.jsx";
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
        const { time, date } = formatAuditLogDateTime(log.createdAt);
        return (
          <AdminMobileCard
            key={log.logId}
            isSelected={isSelected}
            onClick={() => onSelectLog?.(log.logId)}
            ariaLabel={`Xem nhật ký ${getAuditActionLabel(log.actionType)} lúc ${time} ${date}`}
          >
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <p className="font-medium text-admin-text">{getAuditActionLabel(log.actionType)}</p>
                <p className="mt-0.5 text-sm tabular-nums text-admin-text-secondary">
                  {time} · {date}
                </p>
              </div>
              <AuditStatusBadge status={log.status} />
            </div>
            <div className="mt-2">
              <AuditActionTypeBadge actionType={log.actionType} />
            </div>
            <dl className="mt-3 grid gap-2 text-sm">
              <div>
                <dt className="text-admin-text-muted">Admin</dt>
                <dd className="font-mono text-xs text-admin-text">{truncateId(log.adminId)}</dd>
              </div>
              <div>
                <dt className="text-admin-text-muted">Đối tượng</dt>
                <dd className="text-admin-text">
                  <span className="text-sm">{getAuditTargetTypeLabel(log.targetType)}</span>
                  <span className="mt-0.5 block font-mono text-xs">{truncateId(log.targetId, 16)}</span>
                </dd>
              </div>
            </dl>
          </AdminMobileCard>
        );
      })}
    </AdminMobileCardList>
  );
}
