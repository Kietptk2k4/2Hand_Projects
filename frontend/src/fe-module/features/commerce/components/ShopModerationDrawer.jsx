import { useCallback, useMemo, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import { buildModerateSuccessToast } from "../constants/adminShopModerationConstants";
import { useModerateShop } from "../hooks/useModerateShop";
import { useShopModerationDetail } from "../hooks/useShopModerationDetail.js";
import { AdminShopModerateDialog } from "./AdminShopModerateDialog";
import { ShopModerationDrawerView } from "./ShopModerationDrawerView.jsx";

export function ShopModerationDrawer({ shopId, shop, onClose, onRefresh }) {
  const { canSuspendShop, canCloseShop, canReopenShop } = useContentModerationPermissions();
  const { detail, status: detailStatus, errorMessage: detailErrorMessage } =
    useShopModerationDetail(shopId);
  const [toastMessage, setToastMessage] = useState("");
  const [moderateOpen, setModerateOpen] = useState(false);
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const bumpHistoryRefresh = useCallback(() => {
    setHistoryRefreshToken((value) => value + 1);
  }, []);

  const handleModerateSuccess = useCallback(
    (result, action) => {
      setModerateOpen(false);
      setToastMessage(buildModerateSuccessToast(action, result?.alreadyModerated));
      bumpHistoryRefresh();
      onRefresh?.();
    },
    [bumpHistoryRefresh, onRefresh],
  );

  const { isSubmitting, submitError, submit, clearError } = useModerateShop({
    onSuccess: handleModerateSuccess,
  });

  const previewShop = useMemo(() => {
    if (detail) return detail;
    if (!shop) return null;
    return {
      ...shop,
      totalProductCount: shop.productCount,
      activeProductCount: shop.activeProductCount,
      openOrderCount: 0,
    };
  }, [detail, shop]);

  if (!shopId) return null;

  return (
    <>
      <ShopModerationDrawerView
        shopId={shopId}
        shop={previewShop}
        detailStatus={detailStatus}
        detailErrorMessage={detailErrorMessage}
        canSuspendShop={canSuspendShop}
        canCloseShop={canCloseShop}
        canReopenShop={canReopenShop}
        historyRefreshToken={historyRefreshToken}
        disabled={isSubmitting}
        onClose={onClose}
        onModerate={() => setModerateOpen(true)}
      />

      <AdminShopModerateDialog
        open={moderateOpen}
        shop={previewShop}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={() => {
          if (isSubmitting) return;
          setModerateOpen(false);
          clearError();
        }}
        onSubmit={async ({ action, reason }) => {
          if (!shopId) return;
          await submit(shopId, { action, reason });
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
