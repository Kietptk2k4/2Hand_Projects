import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { AdminShopModerateDialog } from "./AdminShopModerateDialog";
import { AdminShopModerationFilters } from "./AdminShopModerationFilters";
import { AdminShopModerationPagination } from "./AdminShopModerationPagination";
import { AdminShopModerationTable } from "./AdminShopModerationTable";
import { buildModerateSuccessToast } from "../constants/adminShopModerationConstants";
import { useAdminShopList } from "../hooks/useAdminShopList";
import { useModerateShop } from "../hooks/useModerateShop";

export function AdminShopModerationTab() {
  const [toastMessage, setToastMessage] = useState("");
  const [moderateShop, setModerateShop] = useState(null);

  const {
    items,
    activeStatusTabId,
    changeStatusTab,
    sort,
    changeSort,
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
  } = useAdminShopList();

  const handleModerateSuccess = useCallback(
    (result, action) => {
      setModerateShop(null);
      setToastMessage(buildModerateSuccessToast(action, result?.alreadyModerated));
      refresh();
    },
    [refresh],
  );

  const { isSubmitting, submitError, submit, clearError } = useModerateShop({
    onSuccess: handleModerateSuccess,
  });

  const handleCloseDialog = useCallback(() => {
    if (isSubmitting) return;
    setModerateShop(null);
    clearError();
  }, [clearError, isSubmitting]);

  const handleSubmitModerate = useCallback(
    async ({ action, reason }) => {
      if (!moderateShop?.shopId) return;
      await submit(moderateShop.shopId, { action, reason });
    },
    [moderateShop, submit],
  );

  const disabled = isLoading || isSubmitting;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 className="text-headline-sm font-semibold text-on-surface">Kiểm duyệt cửa hàng</h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Xem, tạm ngưng hoặc khôi phục shop trên marketplace.
          </p>
        </div>
        <button
          type="button"
          disabled
          title="Sắp có"
          className="inline-flex cursor-not-allowed items-center gap-2 rounded-lg bg-primary/40 px-4 py-2 text-label-md font-medium text-on-primary opacity-70"
        >
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            add
          </span>
          Mời shop mới
        </button>
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
        <>
          <AdminShopModerationFilters
            activeStatusTabId={activeStatusTabId}
            onStatusChange={changeStatusTab}
            sort={sort}
            onSortChange={changeSort}
            searchInput={searchInput}
            onSearchInputChange={setSearchInput}
            onSearchSubmit={applySearch}
            disabled={disabled}
          />

          <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
            {isLoading ? (
              <div className="space-y-0 divide-y divide-outline-variant">
                {[1, 2, 3, 4].map((key) => (
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
                <span className="material-symbols-outlined mb-2 text-4xl text-outline">storefront</span>
                <p className="text-body-md text-on-surface-variant">Không có shop phù hợp bộ lọc.</p>
              </div>
            ) : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <AdminShopModerationTable
                items={items}
                disabled={disabled}
                onModerate={setModerateShop}
              />
            ) : null}

            {!isLoading && !errorMessage && pagination ? (
              <AdminShopModerationPagination
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
        </>
      )}

      <AdminShopModerateDialog
        open={Boolean(moderateShop)}
        shop={moderateShop}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseDialog}
        onSubmit={handleSubmitModerate}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </div>
  );
}
