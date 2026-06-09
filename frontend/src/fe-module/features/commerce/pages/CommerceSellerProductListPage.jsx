import { useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { SellerProductConfirmDialog } from "../components/SellerProductConfirmDialog";
import { SellerProductEmptyState } from "../components/SellerProductEmptyState";
import { SellerProductStatusTabs } from "../components/SellerProductStatusTabs";
import { SellerProductSummaryCards } from "../components/SellerProductSummaryCards";
import { SellerProductTable } from "../components/SellerProductTable";
import { useRouteToastMessage } from "../hooks/useRouteToastMessage";
import { useSellerProductActions } from "../hooks/useSellerProductActions";
import { useSellerProductList } from "../hooks/useSellerProductList";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceSellerProductListPage() {
  const navigate = useNavigate();
  const { toastMessage, setToastMessage, dismissToast } = useRouteToastMessage();

  const {
    items,
    summary,
    pagination,
    page,
    activeTabId,
    changeStatusFilter,
    goToPage,
    searchInput,
    setSearchInput,
    isLoading,
    isEmpty,
    errorMessage,
    rangeStart,
    rangeEnd,
    total,
    reload,
  } = useSellerProductList();

  const handleActionSuccess = useCallback(
    (message) => {
      if (message) setToastMessage(message);
      reload();
    },
    [reload],
  );

  const {
    pending,
    isActing,
    actionError,
    requestAction,
    cancelAction,
    confirmAction,
  } = useSellerProductActions({ onSuccess: handleActionSuccess });

  const handleEdit = useCallback(
    (product) => {
      const path = APP_ROUTES.commerceSellerProductEdit.replace(
        ":productId",
        product.productId,
      );
      navigate(path);
    },
    [navigate],
  );

  return (
    <CommerceShell onComingSoon={() => setToastMessage("Tính năng đang được phát triển.")}>
      <div className="mx-auto w-full max-w-[1280px]">
        <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-headline-md font-bold text-on-surface md:text-headline-lg">
              Quản lý sản phẩm
            </h1>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              Theo dõi, đăng bán và quản lý kho sản phẩm của shop.
            </p>
          </div>
          <Link
            to={APP_ROUTES.commerceSellerProductCreate}
            className="inline-flex items-center gap-1 rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
          >
            <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
              add
            </span>
            Thêm sản phẩm mới
          </Link>
        </header>

        {summary ? <SellerProductSummaryCards summary={summary} /> : null}

        <SellerProductStatusTabs
              activeTabId={activeTabId}
              onChange={changeStatusFilter}
              disabled={isLoading}
            />

            <div className="mb-4 flex flex-wrap items-center gap-3">
              <div className="relative min-w-[240px] flex-1">
                <span
                  className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant"
                  aria-hidden="true"
                >
                  search
                </span>
                <input
                  type="search"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  placeholder="Tìm kiếm tên sản phẩm, SKU..."
                  className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2.5 pl-10 pr-3 text-body-md focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                />
              </div>
              <button
                type="button"
                onClick={() => setToastMessage("Bộ lọc nâng cao sẽ có trong bản cập nhật.")}
                className="rounded-lg border border-outline-variant px-4 py-2.5 text-label-md text-on-surface hover:bg-surface-container-low"
              >
                Lọc
              </button>
            </div>

            {isLoading && items.length === 0 ? (
              <div className="h-48 animate-pulse rounded-xl border border-outline-variant bg-surface-container-low" />
            ) : null}

            {!isLoading && errorMessage && items.length === 0 ? (
              <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
                <p className="text-sm text-on-error-container">{errorMessage}</p>
                <button
                  type="button"
                  onClick={reload}
                  className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
                >
                  Thử lại
                </button>
              </div>
            ) : null}

            {!isLoading && !errorMessage && isEmpty ? <SellerProductEmptyState /> : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <>
                <SellerProductTable
                  items={items}
                  disabled={isActing}
                  onAction={requestAction}
                  onEdit={handleEdit}
                />

                <div className="mt-4 flex flex-wrap items-center justify-between gap-4 text-body-sm text-on-surface-variant">
                  <p>
                    Hiển thị {rangeStart}–{rangeEnd} của {total} sản phẩm
                  </p>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      disabled={page <= 1 || isLoading}
                      onClick={() => goToPage(page - 1)}
                      className="rounded-lg border border-outline-variant px-3 py-1.5 disabled:opacity-50"
                    >
                      Trước
                    </button>
                    <span className="px-2">
                      {page} / {pagination?.totalPages || 1}
                    </span>
                    <button
                      type="button"
                      disabled={!pagination?.hasNext || isLoading}
                      onClick={() => goToPage(page + 1)}
                      className="rounded-lg border border-outline-variant px-3 py-1.5 disabled:opacity-50"
                    >
                      Tiếp
                    </button>
                  </div>
                </div>
              </>
            ) : null}
      </div>

      <SellerProductConfirmDialog
        pending={pending}
        isActing={isActing}
        errorMessage={actionError}
        onCancel={cancelAction}
        onConfirm={confirmAction}
      />

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
