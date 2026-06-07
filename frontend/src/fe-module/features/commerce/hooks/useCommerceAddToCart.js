import { useCallback, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useCartBadge } from "../context/CartBadgeContext";
import { useAddProductToCart } from "./useAddProductToCart";

const SUCCESS_MESSAGE = "Đã thêm vào giỏ hàng.";

export function useCommerceAddToCart({ onSuccess, onError } = {}) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuthSession();
  const { refetch: refetchCartBadge } = useCartBadge();
  const { submit, isSubmitting } = useAddProductToCart();
  const [addingProductId, setAddingProductId] = useState(null);
  const [lastError, setLastError] = useState("");

  const addToCart = useCallback(
    async (productId, quantity = 1) => {
      if (!productId) return null;

      if (!isAuthenticated) {
        const returnTo = `${location.pathname}${location.search}`;
        navigate(
          `${APP_ROUTES.login}?redirectUrl=${encodeURIComponent(returnTo)}`
        );
        return null;
      }

      setAddingProductId(productId);
      setLastError("");
      try {
        const result = await submit({ productId, quantity });
        await refetchCartBadge();
        onSuccess?.(SUCCESS_MESSAGE, result);
        return result;
      } catch (error) {
        const message = error?.message || "Không thêm được sản phẩm vào giỏ.";
        setLastError(message);
        onError?.(message);
        return null;
      } finally {
        setAddingProductId(null);
      }
    },
    [
      isAuthenticated,
      location.pathname,
      location.search,
      navigate,
      onError,
      onSuccess,
      refetchCartBadge,
      submit,
    ]
  );

  const isAddingProduct = useCallback(
    (productId) => isSubmitting && addingProductId === productId,
    [addingProductId, isSubmitting]
  );

  return {
    addToCart,
    handleAddToCart: addToCart,
    isAdding: isSubmitting,
    isAddingToCart: isSubmitting,
    isAddingProduct,
    lastError,
  };
}
