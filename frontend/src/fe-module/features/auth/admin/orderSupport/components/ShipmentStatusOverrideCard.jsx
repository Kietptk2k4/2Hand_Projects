import { useEffect, useMemo, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { overrideShipmentStatus } from "../api/orderSupportApi.js";
import {
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_FORCE_HINT,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_NOTICE,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_SUCCESS,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_TITLE,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_UNCHANGED,
} from "../constants/orderSupportUiStrings.js";
import {
  SHIPMENT_STATUS_LABELS,
  getAllowedTargetStatuses,
} from "../constants/shipmentOverrideConstants.js";
import {
  buildShipmentOverrideConfirmMessage,
  resolveShipmentOverrideSubmitError,
  validateShipmentOverrideForm,
} from "../utils/shipmentOverrideFormUtils.js";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

const REASON_MAX = 500;

export function ShipmentStatusOverrideCard({
  shipmentId,
  detail,
  canWriteShipment,
  canForceWriteShipment,
  onSuccess,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const currentStatus = detail?.internal_status || "";
  const carrier = detail?.carrier || "GHN";

  const [status, setStatus] = useState("");
  const [reason, setReason] = useState("");
  const [force, setForce] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const allowedStatuses = useMemo(
    () => getAllowedTargetStatuses({ carrier, currentStatus, force }),
    [carrier, currentStatus, force],
  );

  useEffect(() => {
    const nextDefault = allowedStatuses[0] || currentStatus || "";
    setStatus((prev) => (allowedStatuses.includes(prev) ? prev : nextDefault));
  }, [allowedStatuses, currentStatus, shipmentId]);

  useEffect(() => {
    setReason("");
    setForce(false);
    setFieldErrors({});
    setSubmitError("");
    setIsSubmitting(false);
  }, [shipmentId, currentStatus]);

  if (!canWriteShipment || !detail) {
    return null;
  }

  const onSubmit = async (event) => {
    event.preventDefault();
    const trimmedReason = reason.trim();
    const errors = validateShipmentOverrideForm({
      status,
      reason: trimmedReason,
      force,
      currentStatus,
      carrier,
      canForceWriteShipment,
    });
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    const confirmed = window.confirm(
      buildShipmentOverrideConfirmMessage({ currentStatus, nextStatus: status, force }),
    );
    if (!confirmed) {
      return;
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const result = await overrideShipmentStatus(shipmentId, {
        status,
        reason: trimmedReason,
        force,
      });
      const unchanged = result?.previous_status === result?.current_status;
      onNotify?.({
        variant: "success",
        message: unchanged
          ? ORDER_SUPPORT_SHIPMENT_OVERRIDE_UNCHANGED
          : ORDER_SUPPORT_SHIPMENT_OVERRIDE_SUCCESS,
      });
      onSuccess?.(result);
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const message = resolveShipmentOverrideSubmitError(error);
      setSubmitError(message);
      onNotify?.({ variant: "error", message });
    } finally {
      setIsSubmitting(false);
    }
  };

  const showForceOption = canForceWriteShipment;

  return (
    <AccountCard className="border-amber-200/80 bg-amber-50/40">
      <div className="mb-4">
        <h3 className="text-base font-semibold text-on-surface">{ORDER_SUPPORT_SHIPMENT_OVERRIDE_TITLE}</h3>
        <p className="mt-1 text-sm text-on-surface-variant">{ORDER_SUPPORT_SHIPMENT_OVERRIDE_NOTICE}</p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="text-on-surface-variant">Trạng thái hiện tại:</span>
          <SupportStatusBadge status={currentStatus} />
        </div>

        <div>
          <label htmlFor="shipment-override-status" className="mb-1 block text-sm font-medium text-on-surface">
            Trạng thái mới
          </label>
          <select
            id="shipment-override-status"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
            disabled={isSubmitting}
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-60"
          >
            {allowedStatuses.map((option) => (
              <option key={option} value={option}>
                {SHIPMENT_STATUS_LABELS[option] || option}
                {option === currentStatus ? " (giữ nguyên)" : ""}
              </option>
            ))}
          </select>
          {fieldErrors.status ? (
            <p className="mt-1 text-xs text-error">{fieldErrors.status}</p>
          ) : null}
        </div>

        <div>
          <label htmlFor="shipment-override-reason" className="mb-1 block text-sm font-medium text-on-surface">
            Lý do ghi đè
          </label>
          <textarea
            id="shipment-override-reason"
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            rows={4}
            maxLength={REASON_MAX}
            disabled={isSubmitting}
            placeholder="VD: GHN webhook không về sau 48h, xác nhận qua hotline GHN..."
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-60"
          />
          <div className="mt-1 flex items-center justify-between gap-2 text-xs text-on-surface-variant">
            <span>{reason.trim().length}/{REASON_MAX} ký tự (tối thiểu 10)</span>
            {fieldErrors.reason ? <span className="text-error">{fieldErrors.reason}</span> : null}
          </div>
        </div>

        {showForceOption ? (
          <div className="rounded-lg border border-outline-variant/70 bg-surface-container-lowest p-3">
            <label className="flex items-start gap-2 text-sm text-on-surface">
              <input
                type="checkbox"
                checked={force}
                onChange={(event) => setForce(event.target.checked)}
                disabled={isSubmitting || !canForceWriteShipment}
                className="mt-0.5"
              />
              <span>
                <span className="font-medium">Force override</span>
                <span className="mt-1 block text-xs text-on-surface-variant">
                  {ORDER_SUPPORT_SHIPMENT_OVERRIDE_FORCE_HINT}
                </span>
              </span>
            </label>
            {fieldErrors.force ? (
              <p className="mt-2 text-xs text-error">{fieldErrors.force}</p>
            ) : null}
          </div>
        ) : null}

        {submitError ? <p className="text-sm text-error">{submitError}</p> : null}

        <div className="flex justify-end">
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90 disabled:opacity-60"
          >
            {isSubmitting ? "Đang ghi đè..." : "Ghi đè trạng thái"}
          </button>
        </div>
      </form>
    </AccountCard>
  );
}
