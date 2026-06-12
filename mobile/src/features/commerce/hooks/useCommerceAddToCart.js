import { useCallback, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { cartKeys } from "../api/cartKeys";
import { useAddProductToCart } from "./useAddProductToCart";

const SUCCESS_MESSAGE = "Đã thêm vào giỏ hàng.";

export function useCommerceAddToCart({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const { submit, isSubmitting } = useAddProductToCart();
  const [addingProductId, setAddingProductId] = useState(null);
  const [lastError, setLastError] = useState("");

  const addToCart = useCallback(
    async (productId, quantity = 1) => {
      if (!productId) return null;

      setAddingProductId(productId);
      setLastError("");
      try {
        const result = await submit({ productId, quantity });
        await queryClient.invalidateQueries({ queryKey: cartKeys.detail() });
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
    [onError, onSuccess, queryClient, submit]
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
