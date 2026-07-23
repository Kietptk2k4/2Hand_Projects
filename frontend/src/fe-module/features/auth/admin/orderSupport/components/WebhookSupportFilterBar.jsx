import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { WEBHOOK_PROVIDER_OPTIONS, WEBHOOK_STATUS_OPTIONS } from "../constants/webhookSupportListConstants.js";

export function WebhookSupportFilterBar({
  draftFilters,
  onDraftFiltersChange,
  onApply,
  onClear,
}) {
  const showPayosStatus = draftFilters.provider !== "GHN";

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
      <AdminFilterField label="Nhà cung cấp" htmlFor="webhook-provider">
        <AdminFilterSelect
          id="webhook-provider"
          value={draftFilters.provider}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, provider: event.target.value })
          }
        >
          {WEBHOOK_PROVIDER_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Mã tham chiếu" htmlFor="webhook-reference">
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

      <AdminFilterField label="Tìm kiếm mã" htmlFor="webhook-q">
        <AdminFilterInput
          id="webhook-q"
          type="text"
          value={draftFilters.q}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
          placeholder="Tìm một phần mã tham chiếu"
        />
      </AdminFilterField>

      <AdminFilterField label="Loại sự kiện" htmlFor="webhook-event-type">
        <AdminFilterInput
          id="webhook-event-type"
          type="text"
          value={draftFilters.event_type}
          onChange={(event) =>
            onDraftFiltersChange({ ...draftFilters, event_type: event.target.value })
          }
          placeholder="PAYOS_00 / delivered"
        />
      </AdminFilterField>

      {showPayosStatus ? (
        <AdminFilterField label="Trạng thái xử lý" htmlFor="webhook-status">
          <AdminFilterSelect
            id="webhook-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {WEBHOOK_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      ) : (
        <AdminFilterField label="Ghi chú" htmlFor="webhook-ghn-hint">
          <p id="webhook-ghn-hint" className="text-sm text-admin-text-secondary">
            GHN không có chữ ký webhook.
          </p>
        </AdminFilterField>
      )}

      <AdminFilterField label="Từ ngày" htmlFor="webhook-from">
        <AdminFilterInput
          id="webhook-from"
          type="datetime-local"
          value={draftFilters.from}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, from: event.target.value })}
          className="text-base sm:text-sm"
        />
      </AdminFilterField>

      <AdminFilterField label="Đến ngày" htmlFor="webhook-to">
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
