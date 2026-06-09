import { useCallback, useEffect, useState } from "react";
import { fetchCart, removeCartItem, updateCartItemQuantity } from "../api/cartApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { mapCartResponse } from "../utils/cartMapper";
import { applyValidationToCart } from "../utils/cartValidationMerge";
import { isCartItemInvalid } from "../utils/cartDisplay";
import { useValidateCartItems } from "./useValidateCartItems";
import { useCartBadge } from "../context/CartBadgeContext";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useCart() {
  const { showSessionExpired } = useAuthSession();
  const { syncFromMappedCart } = useCartBadge();
  const { validate } = useValidateCartItems();
  const [cart, setCart] = useState(null);
  const [validation, setValidation] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [isMutating, setIsMutating] = useState(false);
  const [mutatingItemId, setMutatingItemId] = useState(null);

  const syncCart = useCallback(
    async (raw, { cartItemIds } = {}) => {
      let mapped = mapCartResponse(raw);
      if (!mapped?.items?.length) {
        setValidation(null);
        setCart(mapped);
        syncFromMappedCart(mapped);
        return mapped;
      }

      const result = await validate(cartItemIds);
      if (result) {
        mapped = applyValidationToCart(mapped, result);
        setValidation(result);
      }
      setCart(mapped);
      syncFromMappedCart(mapped);
      return mapped;
    },
    [syncFromMappedCart, validate]
  );

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchCart();
      await syncCart(raw);
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được giỏ hàng. Vui lòng thử lại.");
    }
  }, [showSessionExpired, syncCart]);

  useEffect(() => {
    load();
  }, [load]);

  const updateQuantity = useCallback(
    async (cartItemId, nextQuantity) => {
      const item = cart?.items?.find((entry) => entry.cartItemId === cartItemId);
      if (!item || isCartItemInvalid(item)) return;

      const quantity = Number(nextQuantity);
      if (!Number.isInteger(quantity) || quantity <= 0) return;
      if (quantity > item.availableQuantity) return;

      setIsMutating(true);
      setMutatingItemId(cartItemId);
      setErrorMessage("");

      try {
        await updateCartItemQuantity(cartItemId, quantity);
        const raw = await fetchCart();
        await syncCart(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }
        setErrorMessage(error?.message || "Không cập nhật được số lượng.");
        await load();
      } finally {
        setIsMutating(false);
        setMutatingItemId(null);
      }
    },
    [cart?.items, load, showSessionExpired, syncCart]
  );

  const removeItem = useCallback(
    async (cartItemId) => {
      setIsMutating(true);
      setMutatingItemId(cartItemId);
      setErrorMessage("");

      try {
        await removeCartItem(cartItemId);
        const raw = await fetchCart();
        await syncCart(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }
        setErrorMessage(error?.message || "Không xóa được sản phẩm.");
        await load();
      } finally {
        setIsMutating(false);
        setMutatingItemId(null);
      }
    },
    [load, showSessionExpired, syncCart]
  );

  const revalidate = useCallback(
    async (cartItemIds) => {
      if (!cart?.items?.length) return null;
      try {
        const result = await validate(cartItemIds);
        if (result) {
          setCart((prev) => (prev ? applyValidationToCart(prev, result) : prev));
          setValidation(result);
        }
        return result;
      } catch {
        return null;
      }
    },
    [cart?.items?.length, validate]
  );

  return {
    cart,
    validation,
    status,
    errorMessage,
    isLoading: status === "loading",
    isMutating,
    mutatingItemId,
    isEmpty: status === "ready" && cart?.items?.length === 0,
    updateQuantity,
    removeItem,
    retry: load,
    refetch: load,
    revalidate,
  };
}
