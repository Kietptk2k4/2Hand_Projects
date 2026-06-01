import { useCallback, useState } from "react";
import {
  archiveProduct,
  pauseProduct,
  publishProduct,
} from "../api/sellerProductApi";
import { mapSellerProductApiError } from "../constants/sellerProductConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useSellerProductActions({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [pending, setPending] = useState(null);
  const [isActing, setIsActing] = useState(false);
  const [actionError, setActionError] = useState("");

  const requestAction = useCallback((action, product) => {
    setActionError("");
    setPending({ action, product });
  }, []);

  const cancelAction = useCallback(() => {
    if (isActing) return;
    setPending(null);
    setActionError("");
  }, [isActing]);

  const confirmAction = useCallback(async () => {
    if (!pending) return null;

    setIsActing(true);
    setActionError("");

    const { action, product } = pending;
    const productId = product.productId;

    try {
      let result;
      if (action === "publish") {
        result = await publishProduct(productId);
      } else if (action === "pause") {
        result = await pauseProduct(productId);
      } else if (action === "archive") {
        result = await archiveProduct(productId);
      }

      setPending(null);

      let toast = "";
      if (action === "publish") {
        toast = result?.already_published || result?.alreadyPublished
          ? "Sản phẩm đã được đăng bán"
          : "Đăng bán sản phẩm thành công";
      } else if (action === "pause") {
        toast = result?.already_paused || result?.alreadyPaused
          ? "Sản phẩm đã tạm dừng"
          : "Đã tạm dừng sản phẩm";
      } else if (action === "archive") {
        toast = result?.already_archived || result?.alreadyArchived
          ? "Sản phẩm đã được lưu trữ"
          : "Đã lưu trữ sản phẩm";
      }

      onSuccess?.(toast);
      return toast;
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return null;
      }
      setActionError(mapSellerProductApiError(error));
      return null;
    } finally {
      setIsActing(false);
    }
  }, [onSuccess, pending, showSessionExpired]);

  return {
    pending,
    isActing,
    actionError,
    requestAction,
    cancelAction,
    confirmAction,
  };
}
