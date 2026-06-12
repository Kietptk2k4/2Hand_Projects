import { useCallback, useState } from "react";
import { router } from "expo-router";
import { validateCartItems } from "../api/cartApi";
import { ROUTES } from "../../../shared/constants/routes";
import { mapValidateCartItemsResponse } from "../utils/cartMapper";
import { useAddProductToCart } from "./useAddProductToCart";

const CHECKOUT_BLOCKED_MESSAGE =
  "Không thể mua ngay. Sản phẩm có thể đã hết hàng hoặc không còn khả dụng.";

export function useCommerceBuyNow({ onError } = {}) {
  const { submit } = useAddProductToCart();
  const [buyingProductId, setBuyingProductId] = useState(null);

  const buyNow = useCallback(
    async (productId, quantity = 1) => {
      if (!productId) return false;

      setBuyingProductId(productId);
      try {
        const addResult = await submit({ productId, quantity });
        if (!addResult?.cartItemId) {
          onError?.("Không thêm được sản phẩm vào giỏ để thanh toán.");
          return false;
        }

        const rawValidation = await validateCartItems([addResult.cartItemId]);
        const validation = mapValidateCartItemsResponse(rawValidation);
        if (!validation?.canCheckout) {
          onError?.(CHECKOUT_BLOCKED_MESSAGE);
          return false;
        }

        const validIds = validation.validItems.map((entry) => entry.cartItemId);
        if (!validIds.length) {
          onError?.(CHECKOUT_BLOCKED_MESSAGE);
          return false;
        }

        router.push({
          pathname: ROUTES.commerceCheckout,
          params: { cartItemIds: validIds.join(",") },
        });
        return true;
      } catch (error) {
        onError?.(error?.message || "Không thể mua ngay. Vui lòng thử lại.");
        return false;
      } finally {
        setBuyingProductId(null);
      }
    },
    [onError, submit]
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
