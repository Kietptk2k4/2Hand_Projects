import { useCallback, useState } from "react";
import { validateCartItems } from "../api/cartApi";
import { applyValidationToCart } from "../utils/cartValidationMerge";
import { mapValidateCartItemsResponse } from "../utils/cartMapper";

export function useValidateCartItems() {
  const [isValidating, setIsValidating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const validate = useCallback(async (cartItemIds) => {
    setIsValidating(true);
    setErrorMessage("");

    try {
      const raw = await validateCartItems(cartItemIds);
      return mapValidateCartItemsResponse(raw);
    } catch (error) {
      const message = error?.message || "Không kiểm tra được giỏ hàng.";
      setErrorMessage(message);
      throw { ...error, message };
    } finally {
      setIsValidating(false);
    }
  }, []);

  return {
    validate,
    isValidating,
    errorMessage,
    applyValidationToCart,
  };
}
