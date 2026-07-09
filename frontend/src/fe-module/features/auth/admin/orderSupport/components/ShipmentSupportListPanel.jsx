import { useCallback, useEffect, useState } from "react";
import { listShipmentSupport } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { SHIPMENT_LIST_PAGE_SIZE } from "../constants/shipmentListConstants.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import { ShipmentSupportListView } from "./ShipmentSupportListView.jsx";

export function ShipmentSupportListPanel({
  shipmentListFilters,
  onFiltersChange,
  selectedShipmentId,
  onShipmentSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadShipment } = useOrderSupportPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: shipmentListFilters.status || "",
    carrier: shipmentListFilters.carrier || "",
    sort: shipmentListFilters.sort || "updated_at",
  });

  useEffect(() => {
    setDraftFilters({
      status: shipmentListFilters.status || "",
      carrier: shipmentListFilters.carrier || "",
      sort: shipmentListFilters.sort || "updated_at",
    });
  }, [shipmentListFilters]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await listShipmentSupport({
        status: shipmentListFilters.status || undefined,
        carrier: shipmentListFilters.carrier || undefined,
        sort: shipmentListFilters.sort || "updated_at",
        page: Number(shipmentListFilters.page) || 1,
        size: Number(shipmentListFilters.size) || SHIPMENT_LIST_PAGE_SIZE,
      });
      setResult(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT,
        actionLabel: "xem danh sách vận đơn",
        fallbackMessage: "Không tải được danh sách vận đơn.",
      });
    }
  }, [shipmentListFilters, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: SHIPMENT_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      carrier: "",
      sort: "updated_at",
      page: 1,
      size: SHIPMENT_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const currentPage = Number(shipmentListFilters.page) || 1;
  const totalPages = result?.total_pages || 1;
  const activeSort = shipmentListFilters.sort || "updated_at";

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...shipmentListFilters,
      page: nextPage,
      size: SHIPMENT_LIST_PAGE_SIZE,
    });
  };

  return (
    <ShipmentSupportListView
      canReadShipment={canReadShipment}
      status={status}
      errorMessage={errorMessage}
      result={result}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onRetry={fetchList}
      selectedShipmentId={selectedShipmentId}
      onShipmentSelect={onShipmentSelect}
      currentPage={currentPage}
      totalPages={totalPages}
      activeSort={activeSort}
      onPageChange={handlePageChange}
      formatDateTime={formatDateTime}
    />
  );
}
