import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import { fetchAdminProductList } from "../api/adminProductRemovalApi";
import { mapAdminProductRemovalApiError } from "../constants/adminProductRemovalConstants";
import { PRODUCT_MODERATION_LIST_PAGE_SIZE } from "../constants/productModerationListConstants.js";
import { useBulkProductModeration } from "../hooks/useBulkProductModeration.js";
import { useProductModerationStats } from "../hooks/useProductModerationStats.js";
import {
  buildProductModerationQuickFilter,
  removeProductModerationFilterChip,
} from "../utils/productModerationFilterHelpers.js";
import { mapProductModerationListResponse } from "../utils/productModerationListMapper.js";
import { ProductModerationBulkDialog } from "./ProductModerationBulkDialog.jsx";
import { ProductModerationDrawer } from "./ProductModerationDrawer.jsx";
import { ProductModerationListView } from "./ProductModerationListView.jsx";

export function ProductModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedProductId,
  onProductSelect,
  onProductClear,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canRemoveProduct, canRestoreProduct } = useContentModerationPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedProductIds, setSelectedProductIds] = useState([]);
  const [bulkDialogMode, setBulkDialogMode] = useState(null);
  const [toastMessage, setToastMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "NEWEST";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || PRODUCT_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    q: filterQ,
    sort: filterSort,
  });

  const selectionEnabled = canRemoveProduct || canRestoreProduct;
  const { stats, status: statsStatus, refetch: refetchStats } = useProductModerationStats();
  const {
    isSubmitting: isBulkSubmitting,
    submitError: bulkSubmitError,
    execute: executeBulk,
    clearError: clearBulkError,
  } = useBulkProductModeration();

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterQ, filterSort]);

  useEffect(() => {
    setSelectedProductIds([]);
  }, [filterStatus, filterQ, filterSort, filterPage, filterSize]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminProductList({
        page: filterPage,
        limit: filterSize,
        status: filterStatus || undefined,
        q: filterQ || undefined,
        sort: filterSort,
      });
      setResult(mapProductModerationListResponse(data));
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
      setErrorMessage(mapAdminProductRemovalApiError(error));
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
      size: String(PRODUCT_MODERATION_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildProductModerationQuickFilter(preset);
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
    const next = removeProductModerationFilterChip(listFilters, chipKey);
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

  const handleToggleProduct = useCallback((productId) => {
    setSelectedProductIds((current) =>
      current.includes(productId) ? current.filter((id) => id !== productId) : [...current, productId],
    );
  }, []);

  const handleToggleAll = useCallback((pageItems) => {
    const pageIds = pageItems.map((item) => item.productId);
    setSelectedProductIds((current) => {
      const allSelected = pageIds.every((id) => current.includes(id));
      if (allSelected) {
        return current.filter((id) => !pageIds.includes(id));
      }
      return [...new Set([...current, ...pageIds])];
    });
  }, []);

  const handleBulkSubmit = useCallback(
    async ({ mode, reason, note }) => {
      const { succeeded, failed } = await executeBulk({
        productIds: selectedProductIds,
        mode,
        reason,
        note,
      });

      if (succeeded.length) {
        setToastMessage(
          `Đã xử lý ${succeeded.length} sản phẩm${failed.length ? `, ${failed.length} thất bại` : ""}.`,
        );
        setSelectedProductIds(failed.map((item) => item.productId));
        setBulkDialogMode(null);
        clearBulkError();
        refreshAll();
      }
    },
    [clearBulkError, executeBulk, refreshAll, selectedProductIds],
  );

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.totalPages || pagination?.total_pages || 1;
  const items = result?.items || [];
  const selectedProduct = items.find((item) => item.productId === selectedProductId) || null;

  return (
    <>
      <ProductModerationListView
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
        selectedProductId={selectedProductId}
        selectedProductIds={selectedProductIds}
        selectionEnabled={selectionEnabled}
        canRemoveProduct={canRemoveProduct}
        canRestoreProduct={canRestoreProduct}
        bulkSubmitting={isBulkSubmitting}
        onToggleProduct={handleToggleProduct}
        onToggleAll={handleToggleAll}
        onOpenBulkRemove={() => setBulkDialogMode("remove")}
        onOpenBulkRestore={() => setBulkDialogMode("restore")}
        onClearBulkSelection={() => setSelectedProductIds([])}
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
        onRowSelect={(product) => onProductSelect?.(product.productId)}
        drawer={
          selectedProductId ? (
            <ProductModerationDrawer
              productId={selectedProductId}
              product={selectedProduct}
              onClose={onProductClear}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <ProductModerationBulkDialog
        open={Boolean(bulkDialogMode)}
        mode={bulkDialogMode}
        selectedCount={selectedProductIds.length}
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
