import { SHIPMENT_SUPPORT_VIEW_MODES } from "../constants/shipmentSupportListConstants.js";
import {
  navigateToOrderDetail,
  navigateToWebhookLogs,
} from "../utils/supportNavigation.js";
import { ShipmentSupportDrawerView } from "./ShipmentSupportDrawerView.jsx";

export function ShipmentSupportDrawer({
  shipmentId,
  detail,
  loading,
  errorMessage,
  status,
  shipmentView,
  canReadShipment,
  canWriteShipment,
  canForceWriteShipment,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onNavigate,
  onRetry,
  onOverrideSuccess,
  onNotify,
}) {
  return (
    <ShipmentSupportDrawerView
      open={Boolean(shipmentId)}
      shipmentId={shipmentId}
      detail={detail}
      loading={loading}
      errorMessage={errorMessage}
      status={status}
      shipmentView={shipmentView || SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY}
      canReadShipment={canReadShipment}
      canWriteShipment={canWriteShipment}
      canForceWriteShipment={canForceWriteShipment}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onClose={onClose}
      onViewChange={onViewChange}
      onRetry={onRetry}
      onOverrideSuccess={onOverrideSuccess}
      onNotify={onNotify}
      onNavigateToOrder={() => {
        if (!detail?.order_id) return;
        onNavigate?.(navigateToOrderDetail(detail.order_id));
      }}
      onNavigateToWebhook={() => {
        if (!detail?.ghn_order_code) return;
        onNavigate?.(
          navigateToWebhookLogs({
            provider: "GHN",
            reference_id: detail.ghn_order_code,
          }),
        );
      }}
    />
  );
}
