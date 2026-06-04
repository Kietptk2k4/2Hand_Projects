import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function useViewCommerceProduct() {
  const navigate = useNavigate();

  return useCallback(
    (productId) => {
      if (!productId) return;
      navigate(APP_ROUTES.commerceProductDetail.replace(":productId", productId));
    },
    [navigate]
  );
}
