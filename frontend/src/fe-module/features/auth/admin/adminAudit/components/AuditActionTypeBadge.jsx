import {
  getAuditActionLabel,
  isCriticalAuditAction,
  isReadOnlyAuditAction,
} from "../constants/adminAuditActionLabels.js";

const BADGE_CLASS =
  "inline-flex shrink-0 items-center rounded px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide";

export function AuditActionTypeBadge({ actionType }) {
  if (isCriticalAuditAction(actionType)) {
    return (
      <span className={`${BADGE_CLASS} bg-admin-danger/10 text-admin-danger`}>Quan trọng</span>
    );
  }

  if (isReadOnlyAuditAction(actionType)) {
    return (
      <span className={`${BADGE_CLASS} bg-admin-surface-muted text-admin-text-muted`}>Tra cứu</span>
    );
  }

  return null;
}

export function AuditActionCell({ actionType }) {
  return (
    <div className="min-w-0 space-y-1">
      <p className="font-medium text-admin-text">{getAuditActionLabel(actionType)}</p>
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-mono text-[11px] text-admin-text-muted">{actionType}</span>
        <AuditActionTypeBadge actionType={actionType} />
      </div>
    </div>
  );
}
