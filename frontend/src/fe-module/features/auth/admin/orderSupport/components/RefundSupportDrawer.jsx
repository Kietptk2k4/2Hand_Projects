import { REFUND_SUPPORT_VIEW_MODES } from "../constants/refundSupportListConstants.js";
import {
  navigateToOrderDetail,
  navigateToPaymentDetail,
} from "../utils/supportNavigation.js";
import { RefundSupportDrawerView } from "./RefundSupportDrawerView.jsx";

export function RefundSupportDrawer({
  refundRequestId,
  detail,
  loading,
  errorMessage,
  status,
  refundView,
  canReadRefund,
  canApproveRefund,
  actionPending,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onRetry,
  onConfirm,
  onReject,
  onNavigate,
  onCopied,
}) {
  return (
    <RefundSupportDrawerView
      open={Boolean(refundRequestId)}
      refundRequestId={refundRequestId}
      detail={detail}
      loading={loading}
      errorMessage={errorMessage}
      status={status}
      refundView={refundView || REFUND_SUPPORT_VIEW_MODES.SUMMARY}
      canReadRefund={canReadRefund}
      canApproveRefund={canApproveRefund}
      actionPending={actionPending}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onClose={onClose}
      onViewChange={onViewChange}
      onRetry={onRetry}
      onConfirm={onConfirm}
      onReject={onReject}
      onCopied={onCopied}
      onNavigateToOrder={() => {
        if (!detail?.orderId) return;
        onNavigate?.(navigateToOrderDetail(detail.orderId));
      }}
      onNavigateToPayment={() => {
        if (!detail?.paymentId) return;
        onNavigate?.(navigateToPaymentDetail(detail.paymentId, undefined, detail.orderId));
      }}
    />
  );
}
