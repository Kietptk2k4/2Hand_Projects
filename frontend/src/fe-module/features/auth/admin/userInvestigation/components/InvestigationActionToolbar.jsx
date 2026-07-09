import {
  INVESTIGATION_ACTION_BAN,
  INVESTIGATION_ACTION_RESTRICT,
  INVESTIGATION_ACTION_SUSPEND,
} from "../constants/userInvestigationUiStrings.js";
import { InvestigationActionToolbarView } from "./InvestigationActionToolbarView.jsx";

export const INVESTIGATION_TOOLBAR_ACTIONS = [
  {
    id: "restrict",
    label: INVESTIGATION_ACTION_RESTRICT,
    permission: "canRestrict",
    icon: "shield",
  },
  {
    id: "suspend",
    label: INVESTIGATION_ACTION_SUSPEND,
    permission: "canSuspend",
    icon: "pause_circle",
  },
  {
    id: "ban",
    label: INVESTIGATION_ACTION_BAN,
    permission: "canBan",
    icon: "block",
  },
];

export function filterInvestigationToolbarActions(perms) {
  return INVESTIGATION_TOOLBAR_ACTIONS.filter((action) => perms[action.permission]);
}

export function InvestigationActionToolbar({ actions, onAction, disabled = false }) {
  return (
    <InvestigationActionToolbarView
      actions={actions}
      onAction={onAction}
      disabled={disabled}
    />
  );
}
