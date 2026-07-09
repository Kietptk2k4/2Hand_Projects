import { useEffect, useMemo, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { overrideShipmentStatus } from "../api/orderSupportApi.js";
import {
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_SUCCESS,
  ORDER_SUPPORT_SHIPMENT_OVERRIDE_UNCHANGED,
} from "../constants/orderSupportUiStrings.js";
import { getAllowedTargetStatuses } from "../constants/shipmentOverrideConstants.js";
import {
  buildShipmentOverrideConfirmMessage,
  resolveShipmentOverrideSubmitError,
  validateShipmentOverrideForm,
} from "../utils/shipmentOverrideFormUtils.js";
import { ShipmentStatusOverrideCardView } from "./ShipmentStatusOverrideCardView.jsx";

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

  return (
    <ShipmentStatusOverrideCardView
      currentStatus={currentStatus}
      status={status}
      reason={reason}
      force={force}
      allowedStatuses={allowedStatuses}
      fieldErrors={fieldErrors}
      submitError={submitError}
      isSubmitting={isSubmitting}
      showForceOption={canForceWriteShipment}
      onStatusChange={setStatus}
      onReasonChange={setReason}
      onForceChange={setForce}
      onSubmit={onSubmit}
    />
  );
}
