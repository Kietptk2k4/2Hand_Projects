import { useState } from "react";
import { ANNOUNCEMENT_SEVERITIES } from "../constants/systemAnnouncementConstants.js";
import { GENERIC_CANCEL, GENERIC_CREATE, GENERIC_CLOSE } from "../constants/systemOperationsUiStrings.js";

const defaultForm = {
  title: "",
  content: "",
  severity: "INFO",
  pinned: false,
  dismissible: true,
};

export function CreateSystemAnnouncementDrawer({ open, onClose, onSubmit, pending }) {
  const [form, setForm] = useState(defaultForm);

  if (!open) return null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit?.(form);
    setForm(defaultForm);
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button type="button" aria-label="Đóng" className="absolute inset-0 bg-black/40" onClick={onClose} />
      <aside className="relative flex h-full w-full max-w-lg flex-col border-l border-outline-variant bg-surface shadow-xl">
        <div className="flex items-center justify-between border-b border-outline-variant px-6 py-5">
          <h2 className="text-lg font-semibold text-on-surface">Tạo thông báo draft</h2>
          <button type="button" onClick={onClose} className="text-sm text-on-surface-variant">
            {GENERIC_CLOSE}
          </button>
        </div>
        <form onSubmit={handleSubmit} className="flex flex-1 flex-col overflow-y-auto px-6 py-5">
          <div className="space-y-4">
            <div>
              <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Tiêu đề</label>
              <input
                required
                value={form.title}
                onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Nội dung</label>
              <textarea
                required
                rows={6}
                value={form.content}
                onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
                className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Mức độ</label>
              <select
                value={form.severity}
                onChange={(e) => setForm((prev) => ({ ...prev, severity: e.target.value }))}
                className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              >
                {ANNOUNCEMENT_SEVERITIES.map((severity) => (
                  <option key={severity} value={severity}>
                    {severity}
                  </option>
                ))}
              </select>
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.pinned}
                onChange={(e) => setForm((prev) => ({ ...prev, pinned: e.target.checked }))}
              />
              Pin sau khi publish
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.dismissible}
                onChange={(e) => setForm((prev) => ({ ...prev, dismissible: e.target.checked }))}
              />
              Cho phép người dùng dismiss
            </label>
          </div>
          <div className="mt-auto flex justify-end gap-2 border-t border-outline-variant pt-4">
            <button type="button" onClick={onClose} className="rounded-lg border border-outline-variant px-4 py-2 text-sm">
              {GENERIC_CANCEL}
            </button>
            <button
              type="submit"
              disabled={pending}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
            >
              {GENERIC_CREATE}
            </button>
          </div>
        </form>
      </aside>
    </div>
  );
}