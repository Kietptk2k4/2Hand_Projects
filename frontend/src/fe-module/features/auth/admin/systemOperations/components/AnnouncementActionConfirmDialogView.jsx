import { AdminFilterButton } from "../../components/ui";
import { GENERIC_CANCEL } from "../constants/systemOperationsUiStrings.js";
import { SystemOperationsModalShell } from "./ui/SystemOperationsModalShell.jsx";

export function AnnouncementActionConfirmDialogView({
  open,
  title,
  body,
  confirmLabel,
  confirmDanger = false,
  itemTitle,
  pending,
  onClose,
  onConfirm,
}) {
  return (
    <SystemOperationsModalShell
      open={open}
      title={title}
      onClose={onClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={onClose}>
            {GENERIC_CANCEL}
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={pending}
            className={confirmDanger ? "border-admin-danger/30 bg-admin-danger text-white hover:bg-admin-danger/90" : ""}
            onClick={onConfirm}
          >
            {confirmLabel}
          </AdminFilterButton>
        </>
      }
    >
      <p className="text-sm text-admin-text-secondary">{body}</p>
      {itemTitle ? <p className="mt-3 text-sm font-medium text-admin-text">{itemTitle}</p> : null}
    </SystemOperationsModalShell>
  );
}
