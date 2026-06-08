import { useState } from "react";
import { CONFIG_VALUE_TYPES } from "../constants/systemConfigConstants.js";
import { GENERIC_CANCEL, GENERIC_CREATE } from "../constants/systemOperationsUiStrings.js";

const defaultForm = {
  configKey: "",
  configValue: "",
  valueType: "STRING",
  description: "",
  active: true,
  reason: "",
};

export function CreateSystemConfigModal({ open, onClose, onSubmit, pending }) {
  const [form, setForm] = useState(defaultForm);

  if (!open) return null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit?.(form);
    setForm(defaultForm);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button type="button" aria-label="Đóng" className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative z-10 w-full max-w-lg rounded-xl border border-outline-variant bg-surface p-6 shadow-xl">
        <h2 className="text-lg font-semibold text-on-surface">Tạo cấu hình mới</h2>
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Config key</label>
            <input
              required
              value={form.configKey}
              onChange={(e) => setForm((prev) => ({ ...prev, configKey: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Giá trị</label>
            <textarea
              required
              rows={3}
              value={form.configValue}
              onChange={(e) => setForm((prev) => ({ ...prev, configValue: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Kiểu</label>
              <select
                value={form.valueType}
                onChange={(e) => setForm((prev) => ({ ...prev, valueType: e.target.value }))}
                className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              >
                {CONFIG_VALUE_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(e) => setForm((prev) => ({ ...prev, active: e.target.checked }))}
                />
                Kích hoạt ngay
              </label>
            </div>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Mô tả</label>
            <input
              value={form.description}
              onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Lý do thay đổi</label>
            <textarea
              required
              rows={2}
              value={form.reason}
              onChange={(e) => setForm((prev) => ({ ...prev, reason: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
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
      </div>
    </div>
  );
}