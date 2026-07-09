import { AdminStatusBadge } from "../../components/ui";
import {
  getEnforcementActionLabel,
  getEnforcementStatusLabel,
  getUserStatusLabel,
} from "../utils/investigationLabels.js";
import {
  getEnforcementActionVariant,
  getEnforcementStatusVariant,
  getUserStatusVariant,
} from "./ui/investigationStatusVariants.js";

export function UserStatusBadge({ status }) {
  return (
    <AdminStatusBadge variant={getUserStatusVariant(status)}>
      {getUserStatusLabel(status)}
    </AdminStatusBadge>
  );
}

export function EnforcementActionBadge({ actionType }) {
  return (
    <AdminStatusBadge variant={getEnforcementActionVariant(actionType)}>
      {getEnforcementActionLabel(actionType)}
    </AdminStatusBadge>
  );
}

export function EnforcementStatusBadge({ status, possiblyExpired = false }) {
  return (
    <span className="inline-flex flex-col items-start gap-1">
      <AdminStatusBadge variant={getEnforcementStatusVariant(status)}>
        {getEnforcementStatusLabel(status)}
      </AdminStatusBadge>
      {possiblyExpired ? (
        <span className="text-xs font-medium text-admin-warning">Có thể đã hết hạn (chờ job)</span>
      ) : null}
    </span>
  );
}
