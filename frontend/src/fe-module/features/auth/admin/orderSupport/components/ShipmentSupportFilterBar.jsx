import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterSelect,
} from "../../components/ui";
import {
  SHIPMENT_LIST_CARRIER_OPTIONS,
  SHIPMENT_LIST_SORT_OPTIONS,
  SHIPMENT_LIST_STATUS_OPTIONS,
} from "../constants/shipmentListConstants.js";

export function ShipmentSupportFilterBar({ draftFilters, onDraftFiltersChange, onApply, onClear }) {
  return (
    <AdminFilterBar
      onSubmit={onApply}
      actions={
        <>
          <AdminFilterButton type="submit" variant="primary">
            Áp dụng
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onClear}>
            Xóa lọc
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Trạng thái" htmlFor="shipment-support-status">
        <AdminFilterSelect
          id="shipment-support-status"
          value={draftFilters.status}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, status: event.target.value })
          }
        >
          {SHIPMENT_LIST_STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Carrier" htmlFor="shipment-support-carrier">
        <AdminFilterSelect
          id="shipment-support-carrier"
          value={draftFilters.carrier}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, carrier: event.target.value })
          }
        >
          {SHIPMENT_LIST_CARRIER_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Sắp xếp theo" htmlFor="shipment-support-sort">
        <AdminFilterSelect
          id="shipment-support-sort"
          value={draftFilters.sort}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, sort: event.target.value })
          }
        >
          {SHIPMENT_LIST_SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
    </AdminFilterBar>
  );
}
