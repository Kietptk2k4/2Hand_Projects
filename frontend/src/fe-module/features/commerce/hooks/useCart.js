import { useCallback, useEffect, useState } from "react";
import { fetchCart, removeCartItem, updateCartItemQuantity } from "../api/cartApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { mapCartResponse } from "../utils/cartMapper";
import { isCartItemInvalid } from "../utils/cartDisplay";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useCart() {
  const { showSessionExpired } = useAuthSession();
  const [cart, setCart] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [isMutating, setIsMutating] = useState(false);
  const [mutatingItemId, setMutatingItemId] = useState(null);

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchCart();
      setCart(mapCartResponse(raw));
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được giỏ hàng. Vui lòng thử lại.");
    }
  }, [showSessionExpired]);

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
        const raw = await updateCartItemQuantity(cartItemId, quantity);
        setCart(mapCartResponse(raw));
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
    [cart?.items, load, showSessionExpired]
  );

  const removeItem = useCallback(
    async (cartItemId) => {
      setIsMutating(true);
      setMutatingItemId(cartItemId);
      setErrorMessage("");

      try {
        const raw = await removeCartItem(cartItemId);
        setCart(mapCartResponse(raw));
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
    [load, showSessionExpired]
  );

  return {
    cart,
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
  };
}
