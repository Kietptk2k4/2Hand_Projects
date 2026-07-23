import { ORDER_SUPPORT_VIEW_MODES } from "../constants/orderSupportListConstants.js";
import {
  navigateToPaymentDetail,
  navigateToShipmentDetail,
} from "../utils/supportNavigation.js";
import { OrderSupportDrawerView } from "./OrderSupportDrawerView.jsx";

export function OrderSupportDrawer({
  orderId,
  detail,
  loading,
  errorMessage,
  status,
  orderView,
  canReadOrder,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onNavigate,
  onRetry,
}) {
  return (
    <OrderSupportDrawerView
      open={Boolean(orderId)}
      orderId={orderId}
      detail={detail}
      loading={loading}
      errorMessage={errorMessage}
      status={status}
      orderView={orderView || ORDER_SUPPORT_VIEW_MODES.SUMMARY}
      canReadOrder={canReadOrder}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onClose={onClose}
      onViewChange={onViewChange}
      onRetry={onRetry}
      onNavigateToPayment={() => {
        if (!detail?.payment?.payment_id) return;
        onNavigate?.(navigateToPaymentDetail(detail.payment.payment_id, null, detail.order_id));
      }}
      onNavigateToShipment={(shipmentId) =>
        onNavigate?.(navigateToShipmentDetail(shipmentId, null, detail?.order_id))
      }
    />
  );
}
