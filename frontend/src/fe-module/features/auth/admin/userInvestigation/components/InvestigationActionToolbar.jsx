import { useInvestigationPermissions } from "../hooks/useInvestigationPermissions.js";
import {
  INVESTIGATION_ACTION_BAN,
  INVESTIGATION_ACTION_RESTRICT,
  INVESTIGATION_ACTION_SUSPEND,
} from "../constants/userInvestigationUiStrings.js";

const ACTIONS = [
  {
    id: "restrict",
    label: INVESTIGATION_ACTION_RESTRICT,
    permission: "canRestrict",
    className:
      "border border-primary bg-surface text-primary hover:bg-surface-container-low",
    icon: "shield",
  },
  {
    id: "suspend",
    label: INVESTIGATION_ACTION_SUSPEND,
    permission: "canSuspend",
    className: "border border-amber-600 bg-amber-50 text-amber-900 hover:bg-amber-100",
    icon: "pause_circle",
  },
  {
    id: "ban",
    label: INVESTIGATION_ACTION_BAN,
    permission: "canBan",
    className: "bg-error text-on-error hover:opacity-90",
    icon: "block",
  },
];

function ActionIcon({ name }) {
  if (name === "shield") {
    return (
      <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
        <path d="M12 2 4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3Z" />
      </svg>
    );
  }
  if (name === "pause_circle") {
    return (
      <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
        <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm-1 14H9V8h2v8Zm4 0h-2V8h2v8Z" />
      </svg>
    );
  }
  return (
    <svg className="h-[18px] w-[18px]" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2Zm-1 14H9V8h2v8Zm4 0h-2V8h2v8Z" />
    </svg>
  );
}

export function InvestigationActionToolbar({ onAction, disabled = false }) {
  const perms = useInvestigationPermissions();
  const visibleActions = ACTIONS.filter((action) => perms[action.permission]);

  if (visibleActions.length === 0) {
    return null;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {visibleActions.map((action) => (
        <button
          key={action.id}
          type="button"
          disabled={disabled}
          onClick={() => onAction?.(action.id)}
          className={[
            "inline-flex items-center gap-1.5 rounded-lg px-4 py-2 text-sm font-medium shadow-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50",
            action.className,
          ].join(" ")}
        >
          <ActionIcon name={action.icon} />
          {action.label}
        </button>
      ))}
    </div>
  );
}
