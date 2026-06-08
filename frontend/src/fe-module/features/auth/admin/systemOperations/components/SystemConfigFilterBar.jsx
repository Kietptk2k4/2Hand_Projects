import { CONFIG_ACTIVE_FILTER_OPTIONS, CONFIG_VALUE_TYPES } from "../constants/systemConfigConstants.js";
import { APPLY_FILTERS, CLEAR_FILTERS } from "../constants/systemOperationsUiStrings.js";

export function SystemConfigFilterBar({ filters, onApply, onReset }) {
  const handleSubmit = (event) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    onApply?.({
      q: String(formData.get("q") || "").trim(),
      value_type: String(formData.get("value_type") || "").trim(),
      is_active: String(formData.get("is_active") || "").trim(),
      page: "1",
      size: filters?.size || "20",
    });
  };

  return (
    <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <div className="lg:col-span-2">
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Tìm kiếm</label>
        <input
          name="q"
          defaultValue={filters?.q || ""}
          placeholder="config key / mô tả"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Kiểu giá trị</label>
        <select
          name="value_type"
          defaultValue={filters?.value_type || ""}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        >
          <option value="">Tất cả</option>
          {CONFIG_VALUE_TYPES.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trạng thái</label>
        <select
          name="is_active"
          defaultValue={filters?.is_active || ""}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        >
          {CONFIG_ACTIVE_FILTER_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div className="flex items-end gap-2 md:col-span-2 lg:col-span-4">
        <button
          type="submit"
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
        >
          {APPLY_FILTERS}
        </button>
        <button
          type="button"
          onClick={onReset}
          className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
        >
          {CLEAR_FILTERS}
        </button>
      </div>
    </form>
  );
}