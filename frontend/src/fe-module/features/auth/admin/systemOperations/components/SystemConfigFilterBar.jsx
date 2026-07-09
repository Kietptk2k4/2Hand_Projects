import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
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
      <AdminFilterField label="Tìm kiếm" htmlFor="sc-q" className="lg:col-span-2">
        <AdminFilterInput
          id="sc-q"
          name="q"
          type="search"
          className="text-base"
          defaultValue={filters?.q || ""}
          placeholder="config key / mô tả"
        />
      </AdminFilterField>
      <AdminFilterField label="Kiểu giá trị" htmlFor="sc-value-type">
        <AdminFilterSelect
          id="sc-value-type"
          name="value_type"
          className="text-base"
          defaultValue={filters?.value_type || ""}
        >
          <option value="">Tất cả</option>
          {CONFIG_VALUE_TYPES.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
      <AdminFilterField label="Trạng thái" htmlFor="sc-is-active">
        <AdminFilterSelect
          id="sc-is-active"
          name="is_active"
          className="text-base"
          defaultValue={filters?.is_active || ""}
        >
          {CONFIG_ACTIVE_FILTER_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
    </AdminFilterBar>
  );
}
