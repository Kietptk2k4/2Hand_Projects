import { useCallback, useMemo, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import {
  buildRemoveProductSuccessToast,
  buildRestoreProductSuccessToast,
} from "../constants/adminProductRemovalConstants";
import { useProductModerationDetail } from "../hooks/useProductModerationDetail.js";
import { useRemoveProductByAdmin } from "../hooks/useRemoveProductByAdmin";
import { useRestoreProductByAdmin } from "../hooks/useRestoreProductByAdmin";
import { AdminRemoveProductDialog } from "./AdminRemoveProductDialog";
import { AdminRestoreProductDialog } from "./AdminRestoreProductDialog";
import { ProductModerationDrawerView } from "./ProductModerationDrawerView.jsx";

export function ProductModerationDrawer({ productId, product, onClose, onRefresh }) {
  const { canRemoveProduct, canRestoreProduct } = useContentModerationPermissions();
  const { detail, status: detailStatus, errorMessage: detailErrorMessage } =
    useProductModerationDetail(productId);
  const [toastMessage, setToastMessage] = useState("");
  const [removeOpen, setRemoveOpen] = useState(false);
  const [restoreOpen, setRestoreOpen] = useState(false);
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const bumpHistoryRefresh = useCallback(() => {
    setHistoryRefreshToken((value) => value + 1);
  }, []);

  const handleRemoveSuccess = useCallback(
    (result) => {
      setRemoveOpen(false);
      setToastMessage(buildRemoveProductSuccessToast(result));
      bumpHistoryRefresh();
      onRefresh?.();
    },
    [bumpHistoryRefresh, onRefresh],
  );

  const handleRestoreSuccess = useCallback(() => {
    setRestoreOpen(false);
    setToastMessage(buildRestoreProductSuccessToast());
    bumpHistoryRefresh();
    onRefresh?.();
  }, [bumpHistoryRefresh, onRefresh]);

  const { isSubmitting: isRemoving, submitError: removeError, submit: submitRemove, clearError: clearRemoveError } =
    useRemoveProductByAdmin({ onSuccess: handleRemoveSuccess });

  const {
    isSubmitting: isRestoring,
    submitError: restoreError,
    submit: submitRestore,
    clearError: clearRestoreError,
  } = useRestoreProductByAdmin({ onSuccess: handleRestoreSuccess });

  const previewProduct = useMemo(() => {
    if (detail) return detail;
    if (!product) return null;
    return product;
  }, [detail, product]);

  if (!productId) return null;

  return (
    <>
      <ProductModerationDrawerView
        productId={productId}
        product={previewProduct}
        detailStatus={detailStatus}
        detailErrorMessage={detailErrorMessage}
        canRemoveProduct={canRemoveProduct}
        canRestoreProduct={canRestoreProduct}
        historyRefreshToken={historyRefreshToken}
        disabled={isRemoving || isRestoring}
        onClose={onClose}
        onRemove={() => setRemoveOpen(true)}
        onRestore={() => setRestoreOpen(true)}
      />

      <AdminRemoveProductDialog
        open={removeOpen}
        product={previewProduct}
        isSubmitting={isRemoving}
        submitError={removeError}
        onClose={() => {
          if (isRemoving) return;
          setRemoveOpen(false);
          clearRemoveError();
        }}
        onSubmit={async ({ reason, note }) => {
          if (!productId) return;
          await submitRemove(productId, { reason, note });
        }}
      />

      <AdminRestoreProductDialog
        open={restoreOpen}
        product={previewProduct}
        isSubmitting={isRestoring}
        submitError={restoreError}
        onClose={() => {
          if (isRestoring) return;
          setRestoreOpen(false);
          clearRestoreError();
        }}
        onSubmit={async ({ reason, note }) => {
          if (!productId) return;
          await submitRestore(productId, { reason, note });
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
