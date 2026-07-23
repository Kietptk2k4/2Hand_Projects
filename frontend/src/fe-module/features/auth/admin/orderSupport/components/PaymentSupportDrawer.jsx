import { PAYMENT_SUPPORT_VIEW_MODES } from "../constants/paymentSupportListConstants.js";
import {
  navigateToOrderDetail,
  navigateToWebhookLogs,
} from "../utils/supportNavigation.js";
import { PaymentSupportDrawerView } from "./PaymentSupportDrawerView.jsx";

export function PaymentSupportDrawer({
  paymentId,
  detail,
  loading,
  errorMessage,
  status,
  paymentView,
  orderId,
  canReadPayment,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onNavigate,
  onRetry,
}) {
  return (
    <PaymentSupportDrawerView
      open={Boolean(paymentId)}
      paymentId={paymentId}
      detail={detail}
      loading={loading}
      errorMessage={errorMessage}
      status={status}
      paymentView={paymentView || PAYMENT_SUPPORT_VIEW_MODES.SUMMARY}
      orderId={orderId}
      canReadPayment={canReadPayment}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onClose={onClose}
      onViewChange={onViewChange}
      onRetry={onRetry}
      onNavigateToOrder={() => {
        if (!detail?.order_id) return;
        onNavigate?.(navigateToOrderDetail(detail.order_id));
      }}
      onNavigateToWebhookLogs={() => {
        if (!detail?.provider_order_code || detail.payment_method !== "PAYOS") return;
        onNavigate?.(
          navigateToWebhookLogs({
            provider: "PAYOS",
            reference_id: detail.provider_order_code,
          }),
        );
      }}
    />
  );
}
