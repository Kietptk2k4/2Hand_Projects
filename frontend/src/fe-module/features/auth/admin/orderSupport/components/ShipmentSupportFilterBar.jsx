import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  SHIPMENT_LIST_CARRIER_OPTIONS,
  SHIPMENT_LIST_SORT_OPTIONS,
  SHIPMENT_LIST_STATUS_OPTIONS,
} from "../constants/shipmentSupportListConstants.js";

export function ShipmentSupportFilterBar({
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
          <label htmlFor="shipment-support-lookup" className="text-xs font-medium text-admin-text-secondary">
            Tra cứu nhanh theo UUID
          </label>
          <AdminFilterInput
            id="shipment-support-lookup"
            value={lookupValue}
            onChange={(event) => onLookupChange?.(event.target.value)}
            placeholder="Nhập UUID vận đơn…"
            className="mt-1"
          />
          <p className="mt-1 text-xs text-admin-text-muted">
            Dán UUID từ shipment_db hoặc ticket hỗ trợ.
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
        <AdminFilterField label="Tìm kiếm" htmlFor="shipment-support-q">
          <AdminFilterInput
            id="shipment-support-q"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Shipment ID / tracking…"
          />
        </AdminFilterField>

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

        <AdminFilterField label="Đơn vị" htmlFor="shipment-support-carrier">
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

        <AdminFilterField label="Order ID" htmlFor="shipment-support-order-id" className="xl:col-span-2">
          <AdminFilterInput
            id="shipment-support-order-id"
            value={draftFilters.order_id}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, order_id: event.target.value })
            }
            placeholder="UUID đơn hàng"
          />
        </AdminFilterField>

        <AdminFilterField label="Từ ngày" htmlFor="shipment-support-from">
          <AdminFilterInput
            id="shipment-support-from"
            type="datetime-local"
            value={draftFilters.from}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
            className="text-base sm:text-sm"
          />
        </AdminFilterField>

        <AdminFilterField label="Đến ngày" htmlFor="shipment-support-to">
          <AdminFilterInput
            id="shipment-support-to"
            type="datetime-local"
            value={draftFilters.to}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
            className="text-base sm:text-sm"
          />
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
    </div>
  );
}
