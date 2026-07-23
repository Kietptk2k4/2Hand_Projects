import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  PAYMENT_LIST_METHOD_OPTIONS,
  PAYMENT_LIST_RECONCILIATION_OPTIONS,
  PAYMENT_LIST_STATUS_OPTIONS,
} from "../constants/paymentSupportListConstants.js";

export function PaymentSupportFilterBar({
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
          <label htmlFor="payment-support-lookup" className="text-xs font-medium text-admin-text-secondary">
            Tra cứu nhanh theo UUID
          </label>
          <AdminFilterInput
            id="payment-support-lookup"
            value={lookupValue}
            onChange={(event) => onLookupChange?.(event.target.value)}
            placeholder="Nhập UUID thanh toán…"
            className="mt-1"
          />
          <p className="mt-1 text-xs text-admin-text-muted">
            Dán UUID từ payment_db hoặc ticket hỗ trợ.
          </p>
          {lookupError ? <p className="mt-1 text-xs text-red-600">{lookupError}</p> : null}
        </div>
        <AdminFilterButton type="submit" variant="primary" className="min-h-11 shrink-0">
          Tra cứu
        </AdminFilterButton>
      </form>

      <AdminFilterBar
        className="md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6"
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
        <AdminFilterField label="Tìm kiếm" htmlFor="payment-support-q">
          <AdminFilterInput
            id="payment-support-q"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Payment ID…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="payment-support-status">
          <AdminFilterSelect
            id="payment-support-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {PAYMENT_LIST_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Đối soát" htmlFor="payment-support-reconciliation">
          <AdminFilterSelect
            id="payment-support-reconciliation"
            value={draftFilters.reconciliation_status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, reconciliation_status: event.target.value })
            }
          >
            {PAYMENT_LIST_RECONCILIATION_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
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
            {PAYMENT_LIST_METHOD_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Order ID" htmlFor="payment-support-order-id" className="xl:col-span-2">
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

        <AdminFilterField label="Từ ngày" htmlFor="payment-support-from">
          <AdminFilterInput
            id="payment-support-from"
            type="datetime-local"
            value={draftFilters.from}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
            className="text-base sm:text-sm"
          />
        </AdminFilterField>

        <AdminFilterField label="Đến ngày" htmlFor="payment-support-to">
          <AdminFilterInput
            id="payment-support-to"
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
