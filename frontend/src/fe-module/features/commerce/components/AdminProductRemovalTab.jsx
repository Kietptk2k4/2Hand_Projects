import { useCallback, useMemo, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import {
  buildRemoveProductSuccessToast,
  buildRestoreProductSuccessToast,
} from "../constants/adminProductRemovalConstants";
import { useAdminProductList } from "../hooks/useAdminProductList";
import { useRemoveProductByAdmin } from "../hooks/useRemoveProductByAdmin";
import { useRestoreProductByAdmin } from "../hooks/useRestoreProductByAdmin";
import { AdminProductModerationHistoryPanel } from "./AdminProductModerationHistoryPanel";
import { AdminProductRemovalTabView } from "./AdminProductRemovalTabView";
import { AdminRemoveProductDialog } from "./AdminRemoveProductDialog";
import { AdminRemovedProductCaseDialog } from "./AdminRemovedProductCaseDialog";
import { AdminRestoreProductDialog } from "./AdminRestoreProductDialog";

export function AdminProductRemovalTab({
  productId: urlProductId = "",
  productView = "list",
  onProductViewChange,
}) {
  const [toastMessage, setToastMessage] = useState("");
  const [removeProduct, setRemoveProduct] = useState(null);
  const [restoreProduct, setRestoreProduct] = useState(null);
  const [caseProduct, setCaseProduct] = useState(null);

  const {
    items,
    activeStatusTabId,
    changeStatusTab,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    forbidden,
    isEmpty,
    goToPage,
    retry,
    refresh,
    searchInput,
    setSearchInput,
    applySearch,
  } = useAdminProductList();

  const historyProduct = useMemo(() => {
    if (!urlProductId) return null;
    return items.find((item) => item.productId === urlProductId) ?? { productId: urlProductId };
  }, [items, urlProductId]);

  const handleRemoveSuccess = useCallback(
    (result) => {
      setRemoveProduct(null);
      setToastMessage(buildRemoveProductSuccessToast(result));
      refresh();
    },
    [refresh],
  );

  const handleRestoreSuccess = useCallback(() => {
    setRestoreProduct(null);
    setCaseProduct(null);
    setToastMessage(buildRestoreProductSuccessToast());
    onProductViewChange?.({ productId: undefined, productView: "list" });
    refresh();
  }, [onProductViewChange, refresh]);

  const {
    isSubmitting: isRemoving,
    submitError: removeError,
    submit: submitRemove,
    clearError: clearRemoveError,
  } = useRemoveProductByAdmin({ onSuccess: handleRemoveSuccess });

  const {
    isSubmitting: isRestoring,
    submitError: restoreError,
    submit: submitRestore,
    clearError: clearRestoreError,
  } = useRestoreProductByAdmin({ onSuccess: handleRestoreSuccess });

  const handleCloseRemoveDialog = useCallback(() => {
    if (isRemoving) return;
    setRemoveProduct(null);
    clearRemoveError();
  }, [clearRemoveError, isRemoving]);

  const handleCloseRestoreDialog = useCallback(() => {
    if (isRestoring) return;
    setRestoreProduct(null);
    clearRestoreError();
  }, [clearRestoreError, isRestoring]);

  const handleSubmitRemove = useCallback(
    async ({ reason }) => {
      if (!removeProduct?.productId) return;
      await submitRemove(removeProduct.productId, { reason });
    },
    [removeProduct, submitRemove],
  );

  const handleSubmitRestore = useCallback(
    async ({ reason }) => {
      const target = restoreProduct || caseProduct;
      if (!target?.productId) return;
      await submitRestore(target.productId, { reason });
    },
    [caseProduct, restoreProduct, submitRestore],
  );

  const handleViewHistory = useCallback(
    (product) => {
      onProductViewChange?.({ productId: product.productId, productView: "history" });
    },
    [onProductViewChange],
  );

  const disabled = isLoading || isRemoving || isRestoring;
  const showHistory = productView === "history" && urlProductId;

  return (
    <>
      <AdminProductRemovalTabView
        showHistory={showHistory}
        historyPanel={
          <AdminProductModerationHistoryPanel
            productId={urlProductId}
            productTitle={historyProduct?.title}
            onBack={() => onProductViewChange?.({ productId: undefined, productView: "list" })}
          />
        }
        forbidden={forbidden}
        isLoading={isLoading}
        errorMessage={errorMessage}
        isEmpty={isEmpty}
        items={items}
        pagination={pagination}
        page={page}
        totalPages={totalPages}
        totalItems={totalItems}
        rangeStart={rangeStart}
        rangeEnd={rangeEnd}
        disabled={disabled}
        activeStatusTabId={activeStatusTabId}
        onStatusChange={changeStatusTab}
        searchInput={searchInput}
        onSearchInputChange={setSearchInput}
        onSearchSubmit={applySearch}
        onRemove={setRemoveProduct}
        onRestore={setRestoreProduct}
        onViewCase={setCaseProduct}
        onViewHistory={handleViewHistory}
        onRetry={retry}
        onPrevPage={() => goToPage(page - 1)}
        onNextPage={() => goToPage(page + 1)}
        onGoToPage={goToPage}
      />

      <AdminRemoveProductDialog
        open={Boolean(removeProduct)}
        product={removeProduct}
        isSubmitting={isRemoving}
        submitError={removeError}
        onClose={handleCloseRemoveDialog}
        onSubmit={handleSubmitRemove}
      />

      <AdminRestoreProductDialog
        open={Boolean(restoreProduct)}
        product={restoreProduct}
        isSubmitting={isRestoring}
        submitError={restoreError}
        onClose={handleCloseRestoreDialog}
        onSubmit={handleSubmitRestore}
      />

      <AdminRemovedProductCaseDialog
        open={Boolean(caseProduct)}
        product={caseProduct}
        isRestoring={isRestoring}
        onClose={() => setCaseProduct(null)}
        onRestore={() => {
          if (caseProduct) setRestoreProduct(caseProduct);
        }}
        onViewHistory={() => {
          if (caseProduct) handleViewHistory(caseProduct);
          setCaseProduct(null);
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
