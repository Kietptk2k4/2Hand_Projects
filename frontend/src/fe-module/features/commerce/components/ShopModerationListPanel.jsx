import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import { fetchAdminShopList } from "../api/adminShopModerationApi";
import { mapAdminShopModerationApiError } from "../constants/adminShopModerationConstants";
import { SHOP_MODERATION_LIST_PAGE_SIZE } from "../constants/shopModerationListConstants.js";
import { useBulkShopModeration } from "../hooks/useBulkShopModeration.js";
import { useShopModerationStats } from "../hooks/useShopModerationStats.js";
import {
  buildShopModerationQuickFilter,
  removeShopModerationFilterChip,
} from "../utils/shopModerationFilterHelpers.js";
import { mapShopModerationListResponse } from "../utils/shopModerationListMapper.js";
import { ShopModerationBulkDialog } from "./ShopModerationBulkDialog.jsx";
import { ShopModerationDrawer } from "./ShopModerationDrawer.jsx";
import { ShopModerationListView } from "./ShopModerationListView.jsx";

export function ShopModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedShopId,
  onShopSelect,
  onShopClear,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canSuspendShop, canCloseShop, canReopenShop } = useContentModerationPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedShopIds, setSelectedShopIds] = useState([]);
  const [bulkDialogMode, setBulkDialogMode] = useState(null);
  const [toastMessage, setToastMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "NEWEST";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || SHOP_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    q: filterQ,
    sort: filterSort,
  });

  const selectionEnabled = canSuspendShop || canCloseShop || canReopenShop;
  const { stats, status: statsStatus, refetch: refetchStats } = useShopModerationStats();
  const {
    isSubmitting: isBulkSubmitting,
    submitError: bulkSubmitError,
    execute: executeBulk,
    clearError: clearBulkError,
  } = useBulkShopModeration();

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterQ, filterSort]);

  useEffect(() => {
    setSelectedShopIds([]);
  }, [filterStatus, filterQ, filterSort, filterPage, filterSize]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminShopList({
        page: filterPage,
        limit: filterSize,
        status: filterStatus || undefined,
        q: filterQ || undefined,
        sort: filterSort,
      });
      setResult(mapShopModerationListResponse(data));
      setStatus("ready");
    } catch (error) {
      const code = String(error?.code ?? "");
      if (code === "401" || code.includes("401") || code.includes("COMMERCE-401")) {
        showSessionExpired(error?.message);
        return;
      }
      if (code === "COMMERCE-403") {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        return;
      }
      setStatus("error");
      setErrorMessage(mapAdminShopModerationApiError(error));
    }
  }, [filterPage, filterQ, filterSize, filterSort, filterStatus, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
  }, [fetchList, refetchStats]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...listFilters,
        ...patch,
      });
    },
    [listFilters, onFiltersChange],
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
      status: "",
      q: "",
      sort: "NEWEST",
      page: "1",
      size: String(SHOP_MODERATION_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildShopModerationQuickFilter(preset);
    setDraftFilters({
      status: next.status,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeShopModerationFilterChip(listFilters, chipKey);
    setDraftFilters({
      status: next.status,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleToggleShop = useCallback((shopId) => {
    setSelectedShopIds((current) =>
      current.includes(shopId) ? current.filter((id) => id !== shopId) : [...current, shopId],
    );
  }, []);

  const handleToggleAll = useCallback((pageItems) => {
    const pageIds = pageItems.map((item) => item.shopId);
    setSelectedShopIds((current) => {
      const allSelected = pageIds.every((id) => current.includes(id));
      if (allSelected) {
        return current.filter((id) => !pageIds.includes(id));
      }
      return [...new Set([...current, ...pageIds])];
    });
  }, []);

  const handleBulkSubmit = useCallback(
    async ({ mode, action, reason }) => {
      const { succeeded, failed } = await executeBulk({
        shopIds: selectedShopIds,
        mode,
        action,
        reason,
      });

      if (succeeded.length) {
        setToastMessage(
          `Đã xử lý ${succeeded.length} cửa hàng${failed.length ? `, ${failed.length} thất bại` : ""}.`,
        );
        setSelectedShopIds(failed.map((item) => item.shopId));
        setBulkDialogMode(null);
        clearBulkError();
        refreshAll();
      }
    },
    [clearBulkError, executeBulk, refreshAll, selectedShopIds],
  );

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const items = result?.items || [];
  const selectedShop = items.find((item) => item.shopId === selectedShopId) || null;

  return (
    <>
      <ShopModerationListView
        status={status}
        errorMessage={errorMessage}
        appliedFilters={listFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetry={fetchList}
        stats={stats}
        statsStatus={statsStatus}
        onStatPresetClick={handleQuickFilter}
        items={items}
        pagination={pagination}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        activeSort={filterSort}
        selectedShopId={selectedShopId}
        selectedShopIds={selectedShopIds}
        selectionEnabled={selectionEnabled}
        canSuspendShop={canSuspendShop}
        canReopenShop={canReopenShop}
        bulkSubmitting={isBulkSubmitting}
        onToggleShop={handleToggleShop}
        onToggleAll={handleToggleAll}
        onOpenBulkSuspend={() => setBulkDialogMode("moderate")}
        onOpenBulkRestore={() => setBulkDialogMode("restore")}
        onClearBulkSelection={() => setSelectedShopIds([])}
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
        onRowSelect={(shop) => onShopSelect?.(shop.shopId)}
        drawer={
          selectedShopId ? (
            <ShopModerationDrawer
              shopId={selectedShopId}
              shop={selectedShop}
              onClose={onShopClear}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <ShopModerationBulkDialog
        open={Boolean(bulkDialogMode)}
        mode={bulkDialogMode}
        selectedCount={selectedShopIds.length}
        isSubmitting={isBulkSubmitting}
        submitError={bulkSubmitError}
        onClose={() => {
          if (isBulkSubmitting) return;
          setBulkDialogMode(null);
          clearBulkError();
        }}
        onSubmit={handleBulkSubmit}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
