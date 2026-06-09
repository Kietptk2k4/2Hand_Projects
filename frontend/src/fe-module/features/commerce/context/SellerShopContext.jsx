import { createContext, useContext, useMemo } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useMyShop } from "../hooks/useMyShop";

const SellerShopContext = createContext(null);

export function SellerShopProvider({ children }) {
  const { isAuthenticated } = useAuthSession();
  const { shop, isLoading, isNotFound, isError, errorMessage, reload } = useMyShop({
    enabled: isAuthenticated,
  });

  const value = useMemo(
    () => ({
      shop,
      isSeller: Boolean(shop),
      isLoading: isAuthenticated && isLoading,
      isNotFound: isAuthenticated && isNotFound,
      isError: isAuthenticated && isError,
      errorMessage,
      reload,
    }),
    [shop, isAuthenticated, isLoading, isNotFound, isError, errorMessage, reload],
  );

  return <SellerShopContext.Provider value={value}>{children}</SellerShopContext.Provider>;
}

export function useSellerShop() {
  const context = useContext(SellerShopContext);
  if (!context) {
    throw new Error("useSellerShop must be used inside SellerShopProvider");
  }
  return context;
}
