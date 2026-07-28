import { useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { SellerProductConfirmDialog } from "../components/SellerProductConfirmDialog";
import { SellerProductEmptyState } from "../components/SellerProductEmptyState";
import { SellerProductStatusTabs } from "../components/SellerProductStatusTabs";
import { SellerProductSummaryCards } from "../components/SellerProductSummaryCards";
import { SellerProductTable } from "../components/SellerProductTable";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useRouteToastMessage } from "../hooks/useRouteToastMessage";
import { useSellerProductActions } from "../hooks/useSellerProductActions";
import { useSellerProductList } from "../hooks/useSellerProductList";
import { buildCommerceSellerProductEditPath } from "../utils/commerceRoutes";
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
    [reload]
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
      if (!product?.productId) return;
      navigate(buildCommerceSellerProductEditPath(product.productId));
    },
    [navigate]
  );

  return (
    <CommerceShell onComingSoon={() => setToastMessage("Tính năng đang được phát triển.")}>
      <div className="mx-auto w-full max-w-[1280px]">
        {/* Header */}
        <header className="mb-6 flex flex-wrap items-start justify-between gap-4 border-b border-outline-variant/60 pb-4">
          <div>
            <h1 className="text-xl font-black text-on-surface sm:text-2xl">
              QUẢN LÝ SẢN PHẨM (SELLER)
            </h1>
            <p className="mt-1 text-xs font-semibold text-on-surface-variant">
              Theo dõi, đăng bán và quản lý kho sản phẩm của cửa hàng
            </p>
          </div>
          <Link
            to={APP_ROUTES.commerceSellerProductCreate}
            className="inline-flex items-center gap-1.5 rounded-xl bg-primary px-5 py-3 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 cursor-pointer"
          >
            <span className="material-symbols-outlined text-lg" aria-hidden="true">
              add
            </span>
            Thêm sản phẩm mới
          </Link>
        </header>

        {/* Summary Stat Cards */}
        {summary ? (
          <SellerProductSummaryCards
            summary={summary}
            onSelectStatus={changeStatusFilter}
          />
        ) : null}

        {/* Status Tabs */}
        <SellerProductStatusTabs
          activeTabId={activeTabId}
          onChange={changeStatusFilter}
          disabled={isLoading}
        />

        {/* Search Bar */}
        <div className="mb-6 flex flex-wrap items-center gap-3">
          <div className="relative min-w-[240px] flex-1">
            <span
              className="material-symbols-outlined pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-lg"
              aria-hidden="true"
            >
              search
            </span>
            <input
              type="search"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Tìm kiếm tên sản phẩm, SKU..."
              className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-3 pl-10 pr-4 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
            />
          </div>
          <button
            type="button"
            onClick={() => setToastMessage("Bộ lọc nâng cao sẽ có trong bản cập nhật.")}
            className="rounded-2xl border border-outline-variant bg-surface-container-lowest px-5 py-3 text-xs font-bold text-on-surface hover:bg-surface-container-low shadow-xs cursor-pointer"
          >
            Lọc
          </button>
        </div>

        {/* Loading skeleton */}
        {isLoading && items.length === 0 ? (
          <div className="h-64 animate-pulse rounded-2xl border border-outline-variant bg-surface-container-low" />
        ) : null}

        {/* Error message */}
        {!isLoading && errorMessage && items.length === 0 ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={reload}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {/* Empty state */}
        {!isLoading && !errorMessage && isEmpty ? <SellerProductEmptyState /> : null}

        {/* Data Table */}
        {!isLoading && !errorMessage && items.length > 0 ? (
          <>
            <SellerProductTable
              items={items}
              disabled={isActing}
              onAction={requestAction}
              onEdit={handleEdit}
            />

            {/* Pagination Toolbar */}
            <div className="mt-4 flex flex-wrap items-center justify-between gap-4 text-xs font-semibold text-on-surface-variant">
              <p>
                Hiển thị <span className="font-bold text-on-surface">{rangeStart}–{rangeEnd}</span> của <span className="font-bold text-on-surface">{total}</span> sản phẩm
              </p>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={page <= 1 || isLoading}
                  onClick={() => goToPage(page - 1)}
                  className="rounded-xl border border-outline-variant bg-surface-container-lowest px-4 py-2 text-xs font-bold transition-all hover:bg-surface-container-low disabled:opacity-40 cursor-pointer"
                >
                  Trước
                </button>
                <span className="px-3 text-xs font-bold text-on-surface">
                  {page} / {pagination?.totalPages || 1}
                </span>
                <button
                  type="button"
                  disabled={!pagination?.hasNext || isLoading}
                  onClick={() => goToPage(page + 1)}
                  className="rounded-xl border border-outline-variant bg-surface-container-lowest px-4 py-2 text-xs font-bold transition-all hover:bg-surface-container-low disabled:opacity-40 cursor-pointer"
                >
                  Tiếp
                </button>
              </div>
            </div>
          </>
        ) : null}

        {/* E-Commerce Trust Badges Footer */}
        <CommerceFooterTrustSection />
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
