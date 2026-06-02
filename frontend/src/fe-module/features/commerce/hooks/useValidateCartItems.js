import { useCallback, useState } from "react";
import { validateCartItems } from "../api/cartApi";
import { applyValidationToCart } from "../utils/cartValidationMerge";
import { mapValidateCartItemsResponse } from "../utils/cartMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useValidateCartItems() {
  const { showSessionExpired } = useAuthSession();
  const [isValidating, setIsValidating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const validate = useCallback(
    async (cartItemIds) => {
      setIsValidating(true);
      setErrorMessage("");

      try {
        const raw = await validateCartItems(cartItemIds);
        return mapValidateCartItemsResponse(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return null;
        }
        const message = error?.message || "Không kiểm tra được giỏ hàng.";
        setErrorMessage(message);
        throw { ...error, message };
      } finally {
        setIsValidating(false);
      }
    },
    [showSessionExpired]
  );

  return {
    validate,
    isValidating,
    errorMessage,
    applyValidationToCart,
  };
}
