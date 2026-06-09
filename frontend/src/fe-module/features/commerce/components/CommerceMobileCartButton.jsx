import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useCartBadge } from "../context/CartBadgeContext";
import { useCartFlyTarget } from "../context/CartFlyAnimationContext";
import { CartBadgePill } from "./CartBadgePill";

export function CommerceMobileCartButton({ pulseToken = 0 }) {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const { itemCount } = useCartBadge();
  const setCartTargetRef = useCartFlyTarget("mobile-cart");

  const goToCart = useCallback(() => {
    if (user) {
      navigate(APP_ROUTES.commerceCart);
      return;
    }
    navigate(APP_ROUTES.login);
  }, [navigate, user]);

  if (!user) return null;

  return (
    <button
      type="button"
      ref={setCartTargetRef}
      onClick={goToCart}
      className="fixed right-4 top-[4.5rem] z-40 flex h-11 w-11 items-center justify-center rounded-full border border-outline-variant bg-surface-container-lowest text-on-surface-variant shadow-md transition-colors hover:bg-surface-container-low hover:text-primary lg:hidden"
      aria-label="Giỏ hàng"
    >
      <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
        shopping_cart
      </span>
      <CartBadgePill
        count={itemCount}
        pulseToken={pulseToken}
        className="absolute -right-1 -top-1 min-h-4 min-w-4 px-1 text-[10px] leading-none"
      />
    </button>
  );
}
