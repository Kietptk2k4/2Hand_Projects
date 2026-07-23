import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { listShipmentSupport } from "../api/orderSupportApi.js";
import {
  SHIPMENT_LIST_PAGE_SIZE,
  SHIPMENT_SUPPORT_VIEW_MODES,
} from "../constants/shipmentSupportListConstants.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useShipmentSupportDetail } from "../hooks/useShipmentSupportDetail.js";
import { useShipmentSupportStats } from "../hooks/useShipmentSupportStats.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  buildShipmentSupportQuickFilter,
  removeShipmentSupportFilterChip,
} from "../utils/shipmentSupportFilterHelpers.js";
import { isValidUuid } from "../utils/supportNavigation.js";
import { ShipmentSupportDrawer } from "./ShipmentSupportDrawer.jsx";
import { ShipmentSupportListView } from "./ShipmentSupportListView.jsx";

export function ShipmentSupportListPanel({
  shipmentListFilters,
  onFiltersChange,
  shipmentId,
  shipmentView,
  onShipmentSelectionChange,
  onNavigate,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadShipment, canWriteShipment, canForceWriteShipment } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [lookupValue, setLookupValue] = useState(shipmentId || "");
  const [lookupError, setLookupError] = useState("");

  const filterPage = Number(shipmentListFilters?.page) || 1;
  const filterSize = Number(shipmentListFilters?.size) || SHIPMENT_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: shipmentListFilters?.q || "",
    status: shipmentListFilters?.status || "",
    carrier: shipmentListFilters?.carrier || "",
    order_id: shipmentListFilters?.order_id || "",
    from: shipmentListFilters?.from || "",
    to: shipmentListFilters?.to || "",
    sort: shipmentListFilters?.sort || "updated_at",
  });

  const { stats, status: statsStatus, refetch: refetchStats } = useShipmentSupportStats({
    enabled: canReadShipment,
  });

  const {
    detail,
    status: detailStatus,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = useShipmentSupportDetail(shipmentId, { enabled: canReadShipment && Boolean(shipmentId) });

  useEffect(() => {
    setDraftFilters({
      q: shipmentListFilters?.q || "",
      status: shipmentListFilters?.status || "",
      carrier: shipmentListFilters?.carrier || "",
      order_id: shipmentListFilters?.order_id || "",
      from: shipmentListFilters?.from || "",
      to: shipmentListFilters?.to || "",
      sort: shipmentListFilters?.sort || "updated_at",
    });
  }, [shipmentListFilters]);

  useEffect(() => {
    setLookupValue(shipmentId || "");
  }, [shipmentId]);

  const fetchList = useCallback(async () => {
    if (!canReadShipment) return;

    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await listShipmentSupport({
        q: shipmentListFilters?.q || undefined,
        status: shipmentListFilters?.status || undefined,
        carrier: shipmentListFilters?.carrier || undefined,
        order_id: shipmentListFilters?.order_id || undefined,
        from: shipmentListFilters?.from || undefined,
        to: shipmentListFilters?.to || undefined,
        sort: shipmentListFilters?.sort || "updated_at",
        page: Number(shipmentListFilters?.page) || 1,
        size: Number(shipmentListFilters?.size) || SHIPMENT_LIST_PAGE_SIZE,
      });
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT,
        actionLabel: "xem danh sách vận đơn",
        fallbackMessage: "Không tải được danh sách vận đơn.",
      });
    }
  }, [canReadShipment, shipmentListFilters, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    if (shipmentId) refetchDetail();
  }, [fetchList, shipmentId, refetchDetail, refetchStats]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...shipmentListFilters,
        ...patch,
      });
    },
    [onFiltersChange, shipmentListFilters],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      q: "",
      status: "",
      carrier: "",
      order_id: "",
      from: "",
      to: "",
      sort: "updated_at",
      page: "1",
      size: String(SHIPMENT_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildShipmentSupportQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      status: next.status,
      carrier: next.carrier,
      order_id: next.order_id,
      from: next.from,
      to: next.to,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeShipmentSupportFilterChip(shipmentListFilters, chipKey);
    setDraftFilters({
      q: next.q || "",
      status: next.status || "",
      carrier: next.carrier || "",
      order_id: next.order_id || "",
      from: next.from || "",
      to: next.to || "",
      sort: next.sort || "updated_at",
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleShipmentSelect = (nextShipmentId) => {
    if (!nextShipmentId) return;
    if (nextShipmentId === shipmentId) {
      onShipmentSelectionChange?.({ shipmentId: null, shipmentView: null });
      return;
    }
    onShipmentSelectionChange?.({
      shipmentId: nextShipmentId,
      shipmentView: SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const handleLookupSubmit = (event) => {
    event.preventDefault();
    const trimmed = lookupValue.trim();
    if (!trimmed) {
      onShipmentSelectionChange?.({ shipmentId: null, shipmentView: null });
      setLookupError("");
      return;
    }
    if (!isValidUuid(trimmed)) {
      setLookupError("UUID không hợp lệ.");
      return;
    }
    setLookupError("");
    onShipmentSelectionChange?.({
      shipmentId: trimmed,
      shipmentView: SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const handleOverrideSuccess = () => {
    refreshAll();
    onNotify?.({ variant: "success", message: "Đã cập nhật trạng thái vận đơn." });
  };

  const currentPage = filterPage || listResult?.page || 1;
  const totalPages = listResult?.total_pages || 1;

  return (
    <div className="w-full">
      <ShipmentSupportListView
        canReadShipment={canReadShipment}
        listStatus={listStatus}
        listErrorMessage={listErrorMessage}
        listResult={listResult}
        appliedFilters={shipmentListFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetryList={refreshAll}
        stats={stats}
        statsStatus={statsStatus}
        lookupValue={lookupValue}
        onLookupChange={setLookupValue}
        onLookupSubmit={handleLookupSubmit}
        lookupError={lookupError}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        selectedShipmentId={shipmentId}
        onShipmentSelect={handleShipmentSelect}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: String(nextSize),
          })
        }
        onCopied={() => setToastMessage("Đã sao chép UUID.")}
        drawer={
          shipmentId ? (
            <ShipmentSupportDrawer
              shipmentId={shipmentId}
              detail={detail}
              loading={detailStatus === "loading"}
              errorMessage={detailErrorMessage}
              status={detailStatus}
              shipmentView={shipmentView || SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY}
              canReadShipment={canReadShipment}
              canWriteShipment={canWriteShipment}
              canForceWriteShipment={canForceWriteShipment}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onClose={() => onShipmentSelectionChange?.({ shipmentId: null, shipmentView: null })}
              onViewChange={(nextView) =>
                onShipmentSelectionChange?.({ shipmentId, shipmentView: nextView })
              }
              onNavigate={onNavigate}
              onRetry={refetchDetail}
              onOverrideSuccess={handleOverrideSuccess}
              onNotify={onNotify}
            />
          ) : null
        }
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </div>
  );
}
