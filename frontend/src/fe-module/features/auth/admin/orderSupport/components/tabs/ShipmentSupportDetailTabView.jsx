import { AdminPageHeader } from "../../../components/ui";
import {
  ORDER_SUPPORT_SHIPMENT_SUBTITLE,
  ORDER_SUPPORT_SHIPMENT_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { ShipmentSupportDetailPanel } from "../ShipmentSupportDetailPanel.jsx";
import { ShipmentSupportListPanel } from "../ShipmentSupportListPanel.jsx";

export function ShipmentSupportDetailTabView({
  shipmentId,
  shipmentListFilters,
  onShipmentListFiltersChange,
  onShipmentSelect,
  onNavigate,
  onNotify,
}) {
  return (
    <div className="mx-auto max-w-[1440px] space-y-4">
      <AdminPageHeader title={ORDER_SUPPORT_SHIPMENT_TITLE} subtitle={ORDER_SUPPORT_SHIPMENT_SUBTITLE} />

      <ShipmentSupportListPanel
        shipmentListFilters={shipmentListFilters}
        onFiltersChange={onShipmentListFiltersChange}
        selectedShipmentId={shipmentId}
        onShipmentSelect={onShipmentSelect}
      />

      <ShipmentSupportDetailPanel
        shipmentId={shipmentId}
        onNavigate={onNavigate}
        onNotify={onNotify}
      />
    </div>
  );
}
