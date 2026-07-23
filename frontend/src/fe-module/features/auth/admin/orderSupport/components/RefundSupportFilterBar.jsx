import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  REFUND_LIST_PAYMENT_METHOD_OPTIONS,
  REFUND_LIST_REQUESTED_BY_OPTIONS,
  REFUND_LIST_STATUS_OPTIONS,
} from "../constants/refundSupportListConstants.js";

export function RefundSupportFilterBar({
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
          <label htmlFor="refund-support-lookup" className="text-xs font-medium text-admin-text-secondary">
            Tra cứu nhanh theo UUID
          </label>
          <AdminFilterInput
            id="refund-support-lookup"
            value={lookupValue}
            onChange={(event) => onLookupChange?.(event.target.value)}
            placeholder="Nhập UUID yêu cầu hoàn tiền…"
            className="mt-1"
          />
          <p className="mt-1 text-xs text-admin-text-muted">
            Dán UUID từ ticket hỗ trợ hoặc audit log.
          </p>
          {lookupError ? <p className="mt-1 text-xs text-red-600">{lookupError}</p> : null}
        </div>
        <AdminFilterButton type="submit" variant="primary" className="min-h-11 shrink-0">
          Tra cứu
        </AdminFilterButton>
      </form>

      <AdminFilterBar
        className="md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6"
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
        <AdminFilterField label="Tìm kiếm" htmlFor="refund-support-q">
          <AdminFilterInput
            id="refund-support-q"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Refund / Order / Payment ID…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="refund-support-status">
          <AdminFilterSelect
            id="refund-support-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {REFUND_LIST_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Người yêu cầu" htmlFor="refund-support-requested-by">
          <AdminFilterSelect
            id="refund-support-requested-by"
            value={draftFilters.requested_by}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, requested_by: event.target.value })
            }
          >
            {REFUND_LIST_REQUESTED_BY_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Phương thức" htmlFor="refund-support-payment-method">
          <AdminFilterSelect
            id="refund-support-payment-method"
            value={draftFilters.payment_method}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, payment_method: event.target.value })
            }
          >
            {REFUND_LIST_PAYMENT_METHOD_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Từ ngày" htmlFor="refund-support-from">
          <AdminFilterInput
            id="refund-support-from"
            type="date"
            value={draftFilters.from}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
          />
        </AdminFilterField>

        <AdminFilterField label="Đến ngày" htmlFor="refund-support-to">
          <AdminFilterInput
            id="refund-support-to"
            type="date"
            value={draftFilters.to}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
          />
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
