import { AdminStatusBadge } from "../../components/ui";
import {
  formatOrderStatusLabel,
  formatPaymentMethodLabel,
  formatPaymentStatusLabel,
  formatReconciliationStatusLabel,
  formatShipmentStatusLabel,
} from "../utils/orderSupportDisplayUtils.js";
import { formatRefundStatusLabel } from "../utils/refundSupportFilterHelpers.js";
import { supportStatusVariant } from "./ui/supportStatusVariant.js";

export function SupportStatusBadge({ status, kind = "order", className = "" }) {
  if (!status) return null;

  const label =
    kind === "payment"
      ? formatPaymentStatusLabel(status)
      : kind === "reconciliation"
        ? formatReconciliationStatusLabel(status)
        : kind === "shipment"
          ? formatShipmentStatusLabel(status)
          : kind === "refund"
            ? formatRefundStatusLabel(status)
            : formatOrderStatusLabel(status);

  return (
    <AdminStatusBadge variant={supportStatusVariant(status)} className={className}>
      {label}
    </AdminStatusBadge>
  );
}
