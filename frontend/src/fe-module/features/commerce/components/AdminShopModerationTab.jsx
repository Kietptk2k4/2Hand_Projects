import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { buildModerateSuccessToast } from "../constants/adminShopModerationConstants";
import { useAdminShopList } from "../hooks/useAdminShopList";
import { useModerateShop } from "../hooks/useModerateShop";
import { AdminShopModerateDialog } from "./AdminShopModerateDialog";
import { AdminShopModerationTabView } from "./AdminShopModerationTabView";

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
    <>
      <AdminShopModerationTabView
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
        sort={sort}
        onSortChange={changeSort}
        searchInput={searchInput}
        onSearchInputChange={setSearchInput}
        onSearchSubmit={applySearch}
        onModerate={setModerateShop}
        onRetry={retry}
        onPrevPage={() => goToPage(page - 1)}
        onNextPage={() => goToPage(page + 1)}
        onGoToPage={goToPage}
      />

      <AdminShopModerateDialog
        open={Boolean(moderateShop)}
        shop={moderateShop}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseDialog}
        onSubmit={handleSubmitModerate}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
