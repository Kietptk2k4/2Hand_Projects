import {
  getEnforcementActionClass,
  getEnforcementActionLabel,
  getEnforcementStatusClass,
  getEnforcementStatusLabel,
  getUserStatusClass,
  getUserStatusLabel,
} from "../utils/investigationLabels.js";

export function UserStatusBadge({ status }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold",
        getUserStatusClass(status),
      ].join(" ")}
    >
      <span className="mr-2 h-2 w-2 rounded-full bg-current opacity-70" />
      {getUserStatusLabel(status)}
    </span>
  );
}

export function EnforcementActionBadge({ actionType }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-full px-2 py-1 text-[11px] font-semibold leading-none",
        getEnforcementActionClass(actionType),
      ].join(" ")}
    >
      {getEnforcementActionLabel(actionType)}
    </span>
  );
}

export function EnforcementStatusBadge({ status, possiblyExpired = false }) {
  return (
    <span className="inline-flex flex-col items-start gap-1">
      <span
        className={[
          "inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-semibold",
          getEnforcementStatusClass(status),
        ].join(" ")}
      >
        {getEnforcementStatusLabel(status)}
      </span>
      {possiblyExpired ? (
        <span className="text-xs font-medium text-amber-700">Có thể đã hết hạn (chờ job)</span>
      ) : null}
    </span>
  );
}
