import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  ORDER_LIST_PAYMENT_METHOD_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
  ORDER_LIST_STATUS_OPTIONS,
} from "../constants/orderListConstants.js";

export function OrderSupportFilterBar({ draftFilters, onDraftFiltersChange, onApply, onClear }) {
  return (
    <AdminFilterBar
      onSubmit={onApply}
      actions={
        <>
          <AdminFilterButton type="submit" variant="primary">
            Áp dụng bộ lọc
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onClear}>
            Xóa bộ lọc
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Trạng thái" htmlFor="order-support-status">
        <AdminFilterSelect
          id="order-support-status"
          value={draftFilters.status}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, status: event.target.value })
          }
        >
          {ORDER_LIST_STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Phương thức" htmlFor="order-support-payment-method">
        <AdminFilterSelect
          id="order-support-payment-method"
          value={draftFilters.payment_method}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, payment_method: event.target.value })
          }
        >
          {ORDER_LIST_PAYMENT_METHOD_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Sắp xếp theo" htmlFor="order-support-sort">
        <AdminFilterSelect
          id="order-support-sort"
          value={draftFilters.sort}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, sort: event.target.value })
          }
        >
          {ORDER_LIST_SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Từ (ISO)" htmlFor="order-support-from">
        <AdminFilterInput
          id="order-support-from"
          type="datetime-local"
          value={draftFilters.from}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>

      <AdminFilterField label="Đến (ISO)" htmlFor="order-support-to">
        <AdminFilterInput
          id="order-support-to"
          type="datetime-local"
          value={draftFilters.to}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>
    </AdminFilterBar>
  );
}
