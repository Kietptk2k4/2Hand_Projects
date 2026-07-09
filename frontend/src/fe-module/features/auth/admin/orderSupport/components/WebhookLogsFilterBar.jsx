import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";

const PAYOS_STATUS_OPTIONS = ["", "PROCESSED", "PENDING", "INVALID_SIGNATURE"];

export function WebhookLogsFilterBar({ draftFilters, onDraftFiltersChange, onApply, onClear }) {
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
      <AdminFilterField label="Provider" htmlFor="webhook-provider">
        <AdminFilterSelect
          id="webhook-provider"
          value={draftFilters.provider}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, provider: event.target.value })
          }
        >
          <option value="">Tất cả</option>
          <option value="PAYOS">PayOS</option>
          <option value="GHN">GHN</option>
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Reference ID" htmlFor="webhook-reference" className="lg:col-span-2">
        <AdminFilterInput
          id="webhook-reference"
          type="text"
          value={draftFilters.reference_id}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, reference_id: event.target.value })
          }
          placeholder="payos_order_code / ghn_order_code"
        />
      </AdminFilterField>

      {draftFilters.provider === "PAYOS" || draftFilters.provider === "" ? (
        <AdminFilterField label="Status (PayOS)" htmlFor="webhook-status">
          <AdminFilterSelect
            id="webhook-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {PAYOS_STATUS_OPTIONS.map((option) => (
              <option key={option || "all"} value={option}>
                {option || "Tất cả"}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      ) : null}

      <AdminFilterField label="Từ (ISO)" htmlFor="webhook-from">
        <AdminFilterInput
          id="webhook-from"
          type="datetime-local"
          value={draftFilters.from}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>

      <AdminFilterField label="Đến (ISO)" htmlFor="webhook-to">
        <AdminFilterInput
          id="webhook-to"
          type="datetime-local"
          value={draftFilters.to}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, to: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>
    </AdminFilterBar>
  );
}
