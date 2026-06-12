import { useCallback, useState } from "react";
import { addProductToCart } from "../api/cartApi";
import { mapAddToCartApiError } from "../constants/cartApiError";
import { mapAddProductToCartResponse } from "../utils/cartMapper";

export function useAddProductToCart() {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = useCallback(async ({ productId, quantity = 1 }) => {
    if (!productId) {
      throw { message: "Thiếu thông tin sản phẩm." };
    }

    setIsSubmitting(true);
    try {
      const raw = await addProductToCart({ productId, quantity });
      return mapAddProductToCartResponse(raw);
    } catch (error) {
      throw {
        ...error,
        message: mapAddToCartApiError(error),
      };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  return {
    submit,
    isSubmitting,
  };
}
