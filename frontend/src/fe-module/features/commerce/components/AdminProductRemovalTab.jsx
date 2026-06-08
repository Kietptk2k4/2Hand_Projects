import { useCallback, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { APP_ROUTES } from "../../../shared/constants/routes";
import {
  buildRemoveProductSuccessToast,
  buildRestoreProductSuccessToast,
} from "../constants/adminProductRemovalConstants";
import { useAdminProductList } from "../hooks/useAdminProductList";
import { useRemoveProductByAdmin } from "../hooks/useRemoveProductByAdmin";
import { useRestoreProductByAdmin } from "../hooks/useRestoreProductByAdmin";
import { AdminProductModerationHistoryPanel } from "./AdminProductModerationHistoryPanel";
import { AdminProductRemovalFilters } from "./AdminProductRemovalFilters";
import { AdminProductRemovalPagination } from "./AdminProductRemovalPagination";
import { AdminProductRemovalTable } from "./AdminProductRemovalTable";
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

  const handleBackToList = useCallback(() => {
    onProductViewChange?.({ productId: undefined, productView: "list" });
  }, [onProductViewChange]);

  const disabled = isLoading || isRemoving || isRestoring;
  const showHistory = productView === "history" && urlProductId;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 className="text-headline-sm font-semibold text-on-surface md:text-headline-lg">
            Kiểm duyệt sản phẩm
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant md:text-body-md">
            Rà soát và gỡ listing vi phạm qua admin-service; danh sách vẫn lấy từ Commerce.
          </p>
        </div>
        {!showHistory ? (
          <form
            className="relative w-full md:w-80"
            onSubmit={(e) => {
              e.preventDefault();
              applySearch();
            }}
          >
            <span
              className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-outline"
              aria-hidden="true"
            >
              search
            </span>
            <input
              type="search"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              disabled={disabled}
              placeholder="Tìm kiếm theo tên sản phẩm hoặc Shop ID"
              className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-3 text-body-md shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary disabled:opacity-50"
            />
          </form>
        ) : null}
      </div>

      {showHistory ? (
        <AdminProductModerationHistoryPanel
          productId={urlProductId}
          productTitle={historyProduct?.title}
          onBack={handleBackToList}
        />
      ) : null}

      {!showHistory && forbidden ? (
        <div className="rounded-xl border border-error/30 bg-error-container/40 p-8 text-center">
          <p className="text-body-md text-on-error-container">
            Bạn không có quyền truy cập. Đăng nhập bằng tài khoản admin (
            <span className="font-mono">admin@2hands.vn</span>).
          </p>
          <Link to={APP_ROUTES.login} className="mt-4 inline-block text-primary hover:underline">
            Đăng nhập
          </Link>
        </div>
      ) : null}

      {!showHistory && !forbidden ? (
        <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
          <AdminProductRemovalFilters
            activeStatusTabId={activeStatusTabId}
            onStatusChange={changeStatusTab}
            disabled={disabled}
          />

          {isLoading ? (
            <div className="space-y-0 divide-y divide-outline-variant">
              {[1, 2, 3, 4, 5].map((key) => (
                <div key={key} className="h-20 animate-pulse bg-surface-container-low/60" />
              ))}
            </div>
          ) : null}

          {!isLoading && errorMessage ? (
            <div className="p-8 text-center">
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

          {!isLoading && !errorMessage && isEmpty ? (
            <div className="p-10 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline">
                inventory_2
              </span>
              <p className="text-body-md text-on-surface-variant">
                Không có sản phẩm phù hợp bộ lọc.
              </p>
            </div>
          ) : null}

          {!isLoading && !errorMessage && items.length > 0 ? (
            <AdminProductRemovalTable
              items={items}
              disabled={disabled}
              onRemove={setRemoveProduct}
              onRestore={setRestoreProduct}
              onViewCase={setCaseProduct}
              onViewHistory={handleViewHistory}
            />
          ) : null}

          {!isLoading && !errorMessage && pagination ? (
            <AdminProductRemovalPagination
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
          ) : null}
        </div>
      ) : null}

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
    </div>
  );
}
