import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  ORDER_LIST_PAYMENT_METHOD_OPTIONS,
  ORDER_LIST_PAYMENT_STATUS_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
  ORDER_LIST_STATUS_OPTIONS,
} from "../constants/orderSupportListConstants.js";

export function OrderSupportFilterBar({
  draftFilters,
  onDraftFiltersChange,
  onApply,
  onClear,
  lookupValue,
  onLookupChange,
  onLookupSubmit,
  lookupError,
}) {
  return (
    <div className="space-y-4">
      <form
        onSubmit={onLookupSubmit}
        className="flex flex-col gap-2 rounded-xl border border-admin-border-subtle bg-admin-surface-muted/50 p-3 sm:flex-row sm:items-end"
      >
        <div className="min-w-0 flex-1">
          <label htmlFor="order-support-lookup" className="text-xs font-medium text-admin-text-secondary">
            Tra cứu nhanh theo UUID
          </label>
          <AdminFilterInput
            id="order-support-lookup"
            value={lookupValue}
            onChange={(event) => onLookupChange?.(event.target.value)}
            placeholder="Nhập UUID đơn hàng…"
            className="mt-1"
          />
          <p className="mt-1 text-xs text-admin-text-muted">
            Dán UUID từ commerce_db hoặc ticket hỗ trợ.
          </p>
          {lookupError ? <p className="mt-1 text-xs text-red-600">{lookupError}</p> : null}
        </div>
        <AdminFilterButton type="submit" variant="primary" className="min-h-11 shrink-0">
          Tra cứu
        </AdminFilterButton>
      </form>

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
        <AdminFilterField label="Tìm kiếm" htmlFor="order-support-q">
          <AdminFilterInput
            id="order-support-q"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Order ID hoặc Buyer ID…"
          />
        </AdminFilterField>

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

        <AdminFilterField label="Thanh toán" htmlFor="order-support-payment-status">
          <AdminFilterSelect
            id="order-support-payment-status"
            value={draftFilters.payment_status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, payment_status: event.target.value })
            }
          >
            {ORDER_LIST_PAYMENT_STATUS_OPTIONS.map((option) => (
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

        <AdminFilterField label="Từ ngày" htmlFor="order-support-from">
          <AdminFilterInput
            id="order-support-from"
            type="datetime-local"
            value={draftFilters.from}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
            className="text-base sm:text-sm"
          />
        </AdminFilterField>

        <AdminFilterField label="Đến ngày" htmlFor="order-support-to">
          <AdminFilterInput
            id="order-support-to"
            type="datetime-local"
            value={draftFilters.to}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
            className="text-base sm:text-sm"
          />
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
