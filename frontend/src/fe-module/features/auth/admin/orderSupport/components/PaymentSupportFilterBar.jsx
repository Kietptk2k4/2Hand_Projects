import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";

const PAYMENT_STATUS_OPTIONS = ["", "PENDING", "PAID", "FAILED", "CANCELLED", "EXPIRED"];
const PAYMENT_METHOD_OPTIONS = ["", "COD", "PAYOS"];

export function PaymentSupportFilterBar({ draftFilters, onDraftFiltersChange, onApply, onClear }) {
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
      <AdminFilterField label="Trạng thái" htmlFor="payment-support-status">
        <AdminFilterSelect
          id="payment-support-status"
          value={draftFilters.status}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, status: event.target.value })
          }
        >
          {PAYMENT_STATUS_OPTIONS.map((option) => (
            <option key={option || "all"} value={option}>
              {option || "Tất cả"}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Phương thức" htmlFor="payment-support-method">
        <AdminFilterSelect
          id="payment-support-method"
          value={draftFilters.payment_method}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, payment_method: event.target.value })
          }
        >
          {PAYMENT_METHOD_OPTIONS.map((option) => (
            <option key={option || "all"} value={option}>
              {option || "Tất cả"}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Order ID" htmlFor="payment-support-order-id" className="lg:col-span-2">
        <AdminFilterInput
          id="payment-support-order-id"
          type="text"
          value={draftFilters.order_id}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, order_id: event.target.value })
          }
          placeholder="UUID đơn hàng"
        />
      </AdminFilterField>

      <AdminFilterField label="Từ (ISO)" htmlFor="payment-support-from">
        <AdminFilterInput
          id="payment-support-from"
          type="datetime-local"
          value={draftFilters.from}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>

      <AdminFilterField label="Đến (ISO)" htmlFor="payment-support-to">
        <AdminFilterInput
          id="payment-support-to"
          type="datetime-local"
          value={draftFilters.to}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>
    </AdminFilterBar>
  );
}
