import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { CreateShipmentModal } from "../components/CreateShipmentModal";
import { SellerOrderPagination } from "../components/SellerOrderPagination";
import { SellerShipmentCard } from "../components/SellerShipmentCard";
import { SellerShipmentListEmptyState } from "../components/SellerShipmentListEmptyState";
import { SellerShipmentListHeader } from "../components/SellerShipmentListHeader";
import { SellerShipmentListSkeleton } from "../components/SellerShipmentListSkeleton";
import { SellerShipmentStatusTabs } from "../components/SellerShipmentStatusTabs";
import { SellerShipmentUpdateConfirmDialog } from "../components/SellerShipmentUpdateConfirmDialog";
import { SHIPMENT_STATUS_LABELS } from "../constants/sellerShipmentConstants";
import { updateSellerShipment } from "../api/sellerShipmentApi";
import { mapSellerShipmentApiError } from "../constants/sellerShipmentConstants";
import { mapUpdateShipmentPayload } from "../utils/sellerShipmentMapper";
import { useSellerShipmentList } from "../hooks/useSellerShipmentList";
import { APP_ROUTES } from "../../../shared/constants/routes";

function parsePrefillItemIds(searchParams) {
  const raw = searchParams.get("orderItemIds");
  if (!raw) return [];
  return raw.split(",").map((s) => s.trim()).filter(Boolean);
}

export function CommerceSellerShipmentListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [toastMessage, setToastMessage] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [pendingUpdate, setPendingUpdate] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateError, setUpdateError] = useState("");

  const prefillOrderId = searchParams.get("orderId") || "";
  const prefillOrderItemIds = useMemo(() => parsePrefillItemIds(searchParams), [searchParams]);

  const {
    items,
    activeTabId,
    changeStatusTab,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    noShop,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    refresh,
    clientSearch,
    setClientSearch,
    tabCounts,
  } = useSellerShipmentList();

  useEffect(() => {
    if (searchParams.get("create") === "1") {
      setCreateOpen(true);
    }
  }, [searchParams]);

  const clearCreateQuery = useCallback(() => {
    const next = new URLSearchParams(searchParams);
    next.delete("create");
    next.delete("orderId");
    next.delete("orderItemIds");
    setSearchParams(next, { replace: true });
  }, [searchParams, setSearchParams]);

  const handleCloseCreate = useCallback(() => {
    setCreateOpen(false);
    clearCreateQuery();
  }, [clearCreateQuery]);

  const handleCreateSuccess = useCallback(
    (result) => {
      setCreateOpen(false);
      clearCreateQuery();
      setToastMessage("Tạo vận đơn thành công.");
      refresh();
      if (result?.shipmentId) {
        navigate(
          APP_ROUTES.commerceSellerShipmentDetail.replace(":shipmentId", result.shipmentId),
        );
      }
    },
    [clearCreateQuery, navigate, refresh],
  );

  const handleRequestStatusUpdate = useCallback((item, action) => {
    setUpdateError("");
    setPendingUpdate({ item, action });
  }, []);

  const handleConfirmUpdate = useCallback(async () => {
    if (!pendingUpdate) return;
    setIsUpdating(true);
    setUpdateError("");
    try {
      await updateSellerShipment(
        pendingUpdate.item.shipmentId,
        mapUpdateShipmentPayload({ status: pendingUpdate.action.status }),
      );
      setPendingUpdate(null);
      setToastMessage(
        `Đã cập nhật trạng thái: ${SHIPMENT_STATUS_LABELS[pendingUpdate.action.status] || pendingUpdate.action.status}.`,
      );
      refresh();
    } catch (error) {
      setUpdateError(mapSellerShipmentApiError(error));
    } finally {
      setIsUpdating(false);
    }
  }, [pendingUpdate, refresh]);

  const handleCancelUpdate = useCallback(() => {
    if (isUpdating) return;
    setPendingUpdate(null);
    setUpdateError("");
  }, [isUpdating]);

  const disabled = isLoading || noShop || isUpdating;

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        <SellerShipmentListHeader
          clientSearch={clientSearch}
          onSearchChange={setClientSearch}
          onCreateClick={() => setCreateOpen(true)}
          searchDisabled={disabled}
        />

        {noShop ? (
          <div className="mb-6 rounded-xl border border-outline-variant bg-surface-container-high p-6">
            <p className="text-body-md text-on-surface">
              Bạn chưa có cửa hàng. Tạo shop để quản lý vận chuyển.
            </p>
            <Link
              to={APP_ROUTES.commerceCreateShop}
              className="mt-4 inline-flex rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary"
            >
              Tạo shop
            </Link>
          </div>
        ) : null}

        <SellerShipmentStatusTabs
          activeTabId={activeTabId}
          tabCounts={tabCounts}
          onChange={changeStatusTab}
          disabled={disabled}
        />

        {isLoading ? <SellerShipmentListSkeleton /> : null}

        {!isLoading && errorMessage ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-body-md text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isLoading && !errorMessage && !noShop && (isEmpty || isFilterEmpty || isSearchEmpty) ? (
          <SellerShipmentListEmptyState
            variant={isSearchEmpty ? "search" : isFilterEmpty ? "filter" : "none"}
          />
        ) : null}

        {!isLoading && !errorMessage && !noShop && items.length > 0 ? (
          <>
            <div className="space-y-4">
              {items.map((item) => (
                <SellerShipmentCard
                  key={item.shipmentId}
                  item={item}
                  disabled={disabled}
                  isUpdating={isUpdating}
                  onRequestStatusUpdate={handleRequestStatusUpdate}
                />
              ))}
            </div>
            <SellerOrderPagination
              page={page}
              totalPages={totalPages}
              rangeStart={rangeStart}
              rangeEnd={rangeEnd}
              totalItems={totalItems}
              disabled={disabled}
              onPrev={() => goToPage(page - 1)}
              onNext={() => goToPage(page + 1)}
              onGoToPage={goToPage}
            />
          </>
        ) : null}
      </div>

      <CreateShipmentModal
        open={createOpen}
        onClose={handleCloseCreate}
        onSuccess={handleCreateSuccess}
        prefillOrderId={prefillOrderId}
        prefillOrderItemIds={prefillOrderItemIds}
      />

      <SellerShipmentUpdateConfirmDialog
        open={Boolean(pendingUpdate)}
        title="Cập nhật trạng thái vận đơn"
        description={
          pendingUpdate
            ? `Chuyển vận đơn sang "${pendingUpdate.action.label}"?`
            : ""
        }
        isProcessing={isUpdating}
        errorMessage={updateError}
        onCancel={handleCancelUpdate}
        onConfirm={handleConfirmUpdate}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
