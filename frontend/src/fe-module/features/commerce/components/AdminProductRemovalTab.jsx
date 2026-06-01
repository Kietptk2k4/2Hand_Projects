import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildRemoveProductSuccessToast } from "../constants/adminProductRemovalConstants";
import { useAdminProductList } from "../hooks/useAdminProductList";
import { useRemoveProductByAdmin } from "../hooks/useRemoveProductByAdmin";
import { AdminProductRemovalFilters } from "./AdminProductRemovalFilters";
import { AdminProductRemovalPagination } from "./AdminProductRemovalPagination";
import { AdminProductRemovalTable } from "./AdminProductRemovalTable";
import { AdminRemoveProductDialog } from "./AdminRemoveProductDialog";
import { AdminRemovedProductCaseDialog } from "./AdminRemovedProductCaseDialog";

export function AdminProductRemovalTab() {
  const [toastMessage, setToastMessage] = useState("");
  const [removeProduct, setRemoveProduct] = useState(null);
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

  const handleRemoveSuccess = useCallback(
    (result) => {
      setRemoveProduct(null);
      setToastMessage(buildRemoveProductSuccessToast(result));
      refresh();
    },
    [refresh],
  );

  const { isSubmitting, submitError, submit, clearError } = useRemoveProductByAdmin({
    onSuccess: handleRemoveSuccess,
  });

  const handleCloseRemoveDialog = useCallback(() => {
    if (isSubmitting) return;
    setRemoveProduct(null);
    clearError();
  }, [clearError, isSubmitting]);

  const handleSubmitRemove = useCallback(
    async ({ reason }) => {
      if (!removeProduct?.productId) return;
      await submit(removeProduct.productId, { reason });
    },
    [removeProduct, submit],
  );

  const disabled = isLoading || isSubmitting;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 className="text-headline-sm font-semibold text-on-surface md:text-headline-lg">
            Kiểm duyệt sản phẩm
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant md:text-body-md">
            Rà soát và gỡ listing vi phạm để đảm bảo chất lượng marketplace.
          </p>
        </div>
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
      </div>

      {forbidden ? (
        <div className="rounded-xl border border-error/30 bg-error-container/40 p-8 text-center">
          <p className="text-body-md text-on-error-container">
            Bạn không có quyền truy cập. Đăng nhập bằng tài khoản admin (
            <span className="font-mono">admin@2hands.vn</span>).
          </p>
          <Link to={APP_ROUTES.login} className="mt-4 inline-block text-primary hover:underline">
            Đăng nhập
          </Link>
        </div>
      ) : (
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
              onViewCase={setCaseProduct}
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
      )}

      <AdminRemoveProductDialog
        open={Boolean(removeProduct)}
        product={removeProduct}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseRemoveDialog}
        onSubmit={handleSubmitRemove}
      />

      <AdminRemovedProductCaseDialog
        open={Boolean(caseProduct)}
        product={caseProduct}
        onClose={() => setCaseProduct(null)}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </div>
  );
}
