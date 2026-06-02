import { useCallback, useState } from "react";
import { addProductToCart } from "../api/cartApi";
import { mapAddToCartApiError } from "../constants/cartApiError";
import { mapAddProductToCartResponse } from "../utils/cartMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useAddProductToCart() {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = useCallback(
    async ({ productId, quantity = 1 }) => {
      if (!productId) {
        throw { message: "Thiếu thông tin sản phẩm." };
      }

      setIsSubmitting(true);
      try {
        const raw = await addProductToCart({ productId, quantity });
        return mapAddProductToCartResponse(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        throw {
          ...error,
          message: mapAddToCartApiError(error),
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [showSessionExpired]
  );

  return {
    submit,
    isSubmitting,
  };
}
