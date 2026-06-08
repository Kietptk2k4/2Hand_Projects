import { ANNOUNCEMENT_SEVERITIES, ANNOUNCEMENT_STATUSES } from "../constants/systemAnnouncementConstants.js";
import { APPLY_FILTERS, CLEAR_FILTERS } from "../constants/systemOperationsUiStrings.js";

export function SystemAnnouncementFilterBar({ filters, onApply, onReset }) {
  const handleSubmit = (event) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    onApply?.({
      q: String(formData.get("q") || "").trim(),
      status: String(formData.get("status") || "").trim(),
      severity: String(formData.get("severity") || "").trim(),
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
          placeholder="tiêu đề / nội dung"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trạng thái</label>
        <select
          name="status"
          defaultValue={filters?.status || ""}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm"
        >
          <option value="">Tất cả</option>
          {ANNOUNCEMENT_STATUSES.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Mức độ</label>
        <select
          name="severity"
          defaultValue={filters?.severity || ""}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm"
        >
          <option value="">Tất cả</option>
          {ANNOUNCEMENT_SEVERITIES.map((severity) => (
            <option key={severity} value={severity}>
              {severity}
            </option>
          ))}
        </select>
      </div>
      <div className="flex items-end gap-2 md:col-span-2 lg:col-span-4">
        <button type="submit" className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white">
          {APPLY_FILTERS}
        </button>
        <button
          type="button"
          onClick={onReset}
          className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant"
        >
          {CLEAR_FILTERS}
        </button>
      </div>
    </form>
  );
}