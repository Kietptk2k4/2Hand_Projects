import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchCart } from "../api/cartApi";
import {
  getCartBadgeCountFromMapped,
  getCartBadgeCountFromRaw,
} from "../utils/cartDisplay";

const CartBadgeContext = createContext(null);

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function CartBadgeProvider({ children }) {
  const { isAuthenticated, user, showSessionExpired } = useAuthSession();
  const [itemCount, setItemCount] = useState(0);

  const syncFromMappedCart = useCallback((cart) => {
    setItemCount(getCartBadgeCountFromMapped(cart));
  }, []);

  const refetch = useCallback(async () => {
    if (!isAuthenticated) {
      setItemCount(0);
      return;
    }

    try {
      const raw = await fetchCart();
      setItemCount(getCartBadgeCountFromRaw(raw));
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        setItemCount(0);
      }
    }
  }, [isAuthenticated, showSessionExpired]);

  useEffect(() => {
    if (!isAuthenticated) {
      setItemCount(0);
      return;
    }
    refetch();
  }, [isAuthenticated, user?.id, refetch]);

  const value = useMemo(
    () => ({
      itemCount,
      refetch,
      syncFromMappedCart,
    }),
    [itemCount, refetch, syncFromMappedCart]
  );

  return <CartBadgeContext.Provider value={value}>{children}</CartBadgeContext.Provider>;
}

export function useCartBadge() {
  const context = useContext(CartBadgeContext);
  if (!context) {
    throw new Error("useCartBadge must be used inside CartBadgeProvider");
  }
  return context;
}