import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterSelect,
  AdminSurfaceCard,
} from "../../components/ui";
import {
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_FORCE_HINT,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_NOTICE,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_TITLE,
} from "../constants/orderSupportUiStrings.js";
import { SHIPMENT_STATUS_LABELS } from "../constants/shipmentOverrideConstants.js";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

const REASON_MAX = 500;

export function ShipmentStatusOverrideCardView({
  currentStatus,
  status,
  reason,
  force,
  allowedStatuses,
  fieldErrors,
  submitError,
  isSubmitting,
  showForceOption,
  onStatusChange,
  onReasonChange,
  onForceChange,
  onSubmit,
}) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-warning/30 bg-admin-warning-soft/20">
      <div className="mb-4">
        <h3 className="text-base font-semibold text-admin-text">{ORDER_SUPPORT_SHIPMENT_OVERRIDE_TITLE}</h3>
        <p className="mt-1 text-sm text-admin-text-secondary">{ORDER_SUPPORT_SHIPMENT_OVERRIDE_NOTICE}</p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="text-admin-text-secondary">Trạng thái hiện tại:</span>
          <SupportStatusBadge status={currentStatus} />
        </div>

        <AdminFilterField label="Trạng thái mới" htmlFor="shipment-override-status">
          <AdminFilterSelect
            id="shipment-override-status"
            value={status}
            onChange={(event) => onStatusChange(event.target.value)}
            disabled={isSubmitting}
          >
            {allowedStatuses.map((option) => (
              <option key={option} value={option}>
                {SHIPMENT_STATUS_LABELS[option] || option}
                {option === currentStatus ? " (giữ nguyên)" : ""}
              </option>
            ))}
          </AdminFilterSelect>
          {fieldErrors.status ? (
            <p className="mt-1 text-xs text-admin-danger">{fieldErrors.status}</p>
          ) : null}
        </AdminFilterField>

        <AdminFilterField label="Lý do ghi đè" htmlFor="shipment-override-reason">
          <textarea
            id="shipment-override-reason"
            value={reason}
            onChange={(event) => onReasonChange(event.target.value)}
            rows={4}
            maxLength={REASON_MAX}
            disabled={isSubmitting}
            placeholder="VD: GHN webhook không về sau 48h, xác nhận qua hotline GHN…"
            className="w-full resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-60 sm:text-sm"
          />
          <div className="mt-1 flex flex-wrap items-center justify-between gap-2 text-xs text-admin-text-muted">
            <span>
              {reason.trim().length}/{REASON_MAX} ký tự (tối thiểu 10)
            </span>
            {fieldErrors.reason ? <span className="text-admin-danger">{fieldErrors.reason}</span> : null}
          </div>
        </AdminFilterField>

        {showForceOption ? (
          <div className="rounded-lg border border-admin-border bg-admin-surface-muted/50 p-3">
            <label className="flex min-h-11 items-start gap-2 text-sm text-admin-text">
              <input
                type="checkbox"
                checked={force}
                onChange={(event) => onForceChange(event.target.checked)}
                disabled={isSubmitting}
                className="mt-1"
              />
              <span>
                <span className="font-medium">Force override</span>
                <span className="mt-1 block text-xs text-admin-text-secondary">
                  {ORDER_SUPPORT_SHIPMENT_OVERRIDE_FORCE_HINT}
                </span>
              </span>
            </label>
            {fieldErrors.force ? (
              <p className="mt-2 text-xs text-admin-danger">{fieldErrors.force}</p>
            ) : null}
          </div>
        ) : null}

        {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}

        <div className="flex flex-wrap justify-end gap-2">
          <AdminFilterButton type="submit" variant="primary" disabled={isSubmitting} className="w-full sm:w-auto">
            {isSubmitting ? "Đang ghi đè…" : "Ghi đè trạng thái"}
          </AdminFilterButton>
        </div>
      </form>
    </AdminSurfaceCard>
  );
}
