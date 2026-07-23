import { WEBHOOK_PROCESSING_STATUS_LABELS } from "../constants/webhookSupportListConstants.js";
import {
  navigateToOrderDetail,
  navigateToPaymentDetail,
  navigateToShipmentDetail,
} from "../utils/supportNavigation.js";
import { WebhookSupportDrawerView } from "./WebhookSupportDrawerView.jsx";

export function WebhookSupportDrawer({
  webhookLogId,
  detail,
  loading,
  errorMessage,
  status,
  canReadWebhook,
  formatDateTime,
  onClose,
  onRetry,
  onNavigate,
  onCopied,
}) {
  return (
    <WebhookSupportDrawerView
      open={Boolean(webhookLogId)}
      webhookLogId={webhookLogId}
      detail={detail}
      loading={loading}
      errorMessage={errorMessage}
      status={status}
      canReadWebhook={canReadWebhook}
      formatDateTime={formatDateTime}
      statusLabel={
        detail?.processing_status
          ? WEBHOOK_PROCESSING_STATUS_LABELS[detail.processing_status] || detail.processing_status
          : ""
      }
      onClose={onClose}
      onRetry={onRetry}
      onCopied={onCopied}
      onNavigateToOrder={() => {
        if (!detail?.order_id) return;
        onNavigate?.(navigateToOrderDetail(detail.order_id));
      }}
      onNavigateToPayment={() => {
        if (!detail?.payment_id) return;
        onNavigate?.(navigateToPaymentDetail(detail.payment_id, undefined, detail.order_id));
      }}
      onNavigateToShipment={() => {
        if (!detail?.shipment_id) return;
        onNavigate?.(navigateToShipmentDetail(detail.shipment_id, undefined, detail.order_id));
      }}
    />
  );
}
