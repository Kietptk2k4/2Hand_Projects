import { useCallback, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useCartBadge } from "../context/CartBadgeContext";
import { useAddProductToCart } from "./useAddProductToCart";
import { useValidateCartItems } from "./useValidateCartItems";

const CHECKOUT_BLOCKED_MESSAGE =
  "Không thể mua ngay. Sản phẩm có thể đã hết hàng hoặc không còn khả dụng.";

function toCheckoutCacheItem(addResult) {
  if (!addResult) return null;

  return {
    cartItemId: addResult.cartItemId,
    productId: addResult.productId,
    productName: addResult.product?.productName,
    imageUrl: addResult.product?.imageUrl,
    quantity: addResult.quantity,
  };
}

export function useCommerceBuyNow({ onError } = {}) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuthSession();
  const { refetch: refetchCartBadge } = useCartBadge();
  const { submit } = useAddProductToCart();
  const { validate } = useValidateCartItems();
  const [buyingProductId, setBuyingProductId] = useState(null);

  const buyNow = useCallback(
    async (productId, quantity = 1) => {
      if (!productId) return false;

      if (!isAuthenticated) {
        const returnTo = `${location.pathname}${location.search}`;
        navigate(`${APP_ROUTES.login}?redirectUrl=${encodeURIComponent(returnTo)}`);
        return false;
      }

      setBuyingProductId(productId);
      try {
        const addResult = await submit({ productId, quantity });
        if (!addResult?.cartItemId) {
          onError?.("Không thêm được sản phẩm vào giỏ để thanh toán.");
          return false;
        }

        await refetchCartBadge();

        const validation = await validate([addResult.cartItemId]);
        if (!validation?.canCheckout) {
          onError?.(CHECKOUT_BLOCKED_MESSAGE);
          return false;
        }

        const validIds = validation.validItems.map((entry) => entry.cartItemId);
        if (!validIds.length) {
          onError?.(CHECKOUT_BLOCKED_MESSAGE);
          return false;
        }

        const cacheItem = toCheckoutCacheItem(addResult);

        navigate(APP_ROUTES.commerceCheckout, {
          state: {
            cartItemIds: validIds,
            cartItemsCache: cacheItem ? [cacheItem] : [],
          },
        });
        return true;
      } catch (error) {
        onError?.(error?.message || "Không thể mua ngay. Vui lòng thử lại.");
        return false;
      } finally {
        setBuyingProductId(null);
      }
    },
    [
      isAuthenticated,
      location.pathname,
      location.search,
      navigate,
      onError,
      refetchCartBadge,
      submit,
      validate,
    ]
  );

  const isBuyingProduct = useCallback(
    (productId) => buyingProductId === productId,
    [buyingProductId]
  );

  return {
    buyNow,
    isBuying: buyingProductId !== null,
    isBuyingProduct,
  };
}
