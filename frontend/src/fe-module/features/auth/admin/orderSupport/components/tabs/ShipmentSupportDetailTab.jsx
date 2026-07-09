import { ShipmentSupportDetailTabView } from "./ShipmentSupportDetailTabView.jsx";

export function ShipmentSupportDetailTab({
  shipmentId,
  shipmentListFilters,
  onShipmentListFiltersChange,
  onShipmentSelect,
  onNavigate,
  onNotify,
}) {
  return (
    <ShipmentSupportDetailTabView
      shipmentId={shipmentId}
      shipmentListFilters={shipmentListFilters}
      onShipmentListFiltersChange={onShipmentListFiltersChange}
      onShipmentSelect={onShipmentSelect}
      onNavigate={onNavigate}
      onNotify={onNotify}
    />
  );
}
