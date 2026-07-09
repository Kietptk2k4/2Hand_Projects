import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { ANNOUNCEMENT_SEVERITIES, ANNOUNCEMENT_STATUSES } from "../constants/systemAnnouncementConstants.js";
import { APPLY_FILTERS, CLEAR_FILTERS } from "../constants/systemOperationsUiStrings.js";

const STATUS_LABELS = {
  DRAFT: "Draft",
  SENT: "Đã gửi",
  CANCELLED: "Đã hủy",
};

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
    <AdminFilterBar
      onSubmit={handleSubmit}
      actions={
        <>
          <AdminFilterButton type="submit" variant="primary">
            {APPLY_FILTERS}
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onReset}>
            {CLEAR_FILTERS}
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Tìm kiếm" htmlFor="sa-q" className="lg:col-span-2">
        <AdminFilterInput
          id="sa-q"
          name="q"
          type="search"
          className="text-base"
          defaultValue={filters?.q || ""}
          placeholder="tiêu đề / nội dung"
        />
      </AdminFilterField>
      <AdminFilterField label="Trạng thái" htmlFor="sa-status">
        <AdminFilterSelect
          id="sa-status"
          name="status"
          className="text-base"
          defaultValue={filters?.status || ""}
        >
          <option value="">Tất cả</option>
          {ANNOUNCEMENT_STATUSES.map((status) => (
            <option key={status} value={status}>
              {STATUS_LABELS[status] ?? status}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
      <AdminFilterField label="Mức độ" htmlFor="sa-severity">
        <AdminFilterSelect
          id="sa-severity"
          name="severity"
          className="text-base"
          defaultValue={filters?.severity || ""}
        >
          <option value="">Tất cả</option>
          {ANNOUNCEMENT_SEVERITIES.map((severity) => (
            <option key={severity} value={severity}>
              {severity}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
    </AdminFilterBar>
  );
}
