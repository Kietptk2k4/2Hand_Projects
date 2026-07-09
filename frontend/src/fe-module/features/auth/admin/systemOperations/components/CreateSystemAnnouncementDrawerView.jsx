import { AdminFilterButton, AdminFilterField, AdminFilterInput, AdminFilterSelect } from "../../components/ui";
import { ANNOUNCEMENT_SEVERITIES } from "../constants/systemAnnouncementConstants.js";
import { GENERIC_CANCEL, GENERIC_CREATE } from "../constants/systemOperationsUiStrings.js";
import { SystemOperationsDrawerShell } from "./ui/SystemOperationsDrawerShell.jsx";

export function CreateSystemAnnouncementDrawerView({
  open,
  form,
  pending,
  onFieldChange,
  onClose,
  onSubmit,
}) {
  return (
    <SystemOperationsDrawerShell
      open={open}
      title="Tạo thông báo draft"
      onClose={onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={onClose}>
            {GENERIC_CANCEL}
          </AdminFilterButton>
          <AdminFilterButton type="submit" form="create-system-announcement-form" variant="primary" disabled={pending}>
            {GENERIC_CREATE}
          </AdminFilterButton>
        </>
      }
    >
      <form id="create-system-announcement-form" onSubmit={onSubmit} className="space-y-4">
        <AdminFilterField label="Tiêu đề" htmlFor="create-announcement-title">
          <AdminFilterInput
            id="create-announcement-title"
            required
            className="text-base"
            value={form.title}
            onChange={(e) => onFieldChange({ title: e.target.value })}
          />
        </AdminFilterField>
        <AdminFilterField label="Nội dung" htmlFor="create-announcement-content">
          <textarea
            id="create-announcement-content"
            required
            rows={6}
            className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text placeholder:text-admin-text-muted focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent/20"
            value={form.content}
            onChange={(e) => onFieldChange({ content: e.target.value })}
          />
        </AdminFilterField>
        <AdminFilterField label="Mức độ" htmlFor="create-announcement-severity">
          <AdminFilterSelect
            id="create-announcement-severity"
            className="text-base"
            value={form.severity}
            onChange={(e) => onFieldChange({ severity: e.target.value })}
          >
            {ANNOUNCEMENT_SEVERITIES.map((severity) => (
              <option key={severity} value={severity}>
                {severity}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
        <label className="flex min-h-11 items-center gap-2 text-sm text-admin-text">
          <input
            type="checkbox"
            checked={form.pinned}
            onChange={(e) => onFieldChange({ pinned: e.target.checked })}
            className="h-4 w-4 rounded border-admin-border"
          />
          Pin sau khi publish
        </label>
        <label className="flex min-h-11 items-center gap-2 text-sm text-admin-text">
          <input
            type="checkbox"
            checked={form.dismissible}
            onChange={(e) => onFieldChange({ dismissible: e.target.checked })}
            className="h-4 w-4 rounded border-admin-border"
          />
          Cho phép người dùng dismiss
        </label>
      </form>
    </SystemOperationsDrawerShell>
  );
}
