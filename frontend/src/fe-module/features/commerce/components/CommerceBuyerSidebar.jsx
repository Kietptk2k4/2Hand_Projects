import { useCallback, useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCartBadge } from "../context/CartBadgeContext";
import { useCartFlyTarget } from "../context/CartFlyAnimationContext";
import { useSellerShop } from "../context/SellerShopContext";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { CartBadgePill } from "./CartBadgePill";
import { CommerceSidebarFrame } from "./CommerceSidebarFrame";

const BUYER_LINKS = [
  { id: "home", icon: "home", label: "Trang chủ", route: APP_ROUTES.commerceHome },
  {
    id: "cart",
    icon: "shopping_cart",
    label: "Giỏ hàng",
    route: APP_ROUTES.commerceCart,
    requiresAuth: true,
  },
  {
    id: "orders",
    icon: "receipt_long",
    label: "Đơn hàng",
    route: APP_ROUTES.commerceOrders,
    requiresAuth: true,
  },
  {
    id: "addresses",
    icon: "location_on",
    label: "Địa chỉ giao hàng",
    route: APP_ROUTES.commerceAddresses,
    requiresAuth: true,
  },
];

function isBuyerLinkActive(link, pathname) {
  if (!link.route) return false;
  if (link.id === "home") return pathname === APP_ROUTES.commerceHome;
  if (link.id === "cart") return pathname === APP_ROUTES.commerceCart;
  if (link.id === "orders") {
    return (
      pathname === APP_ROUTES.commerceOrders ||
      pathname.startsWith(`${APP_ROUTES.commerceOrders}/`)
    );
  }
  if (link.id === "addresses") return pathname === APP_ROUTES.commerceAddresses;
  if (link.id === "create-shop") return pathname === APP_ROUTES.commerceCreateShop;
  if (link.id === "seller-hub") {
    return pathname.startsWith("/commerce/seller");
  }
  return pathname === link.route || pathname.startsWith(`${link.route}/`);
}

export function CommerceBuyerSidebar({ onComingSoon, pulseToken = 0 }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { user } = useAuthSession();
  const { isSeller, isLoading: isSellerLoading } = useSellerShop();
  const { itemCount: cartItemCount } = useCartBadge();
  const setSidebarCartTargetRef = useCartFlyTarget("sidebar-cart");

  const extraLinks = useMemo(() => {
    if (!user || isSellerLoading) return [];
    if (isSeller) {
      return [
        {
          id: "seller-hub",
          icon: "storefront",
          label: "Khu bán hàng",
          route: APP_ROUTES.commerceSellerProducts,
          requiresAuth: true,
        },
      ];
    }
    return [
      {
        id: "create-shop",
        icon: "store",
        label: "Đăng ký bán hàng",
        route: APP_ROUTES.commerceCreateShop,
        requiresAuth: true,
      },
    ];
  }, [user, isSeller, isSellerLoading]);

  const links = useMemo(() => [...BUYER_LINKS, ...extraLinks], [extraLinks]);

  const handleLinkClick = useCallback(
    (link) => {
      if (link.comingSoon) {
        onComingSoon?.();
        return;
      }
      if (link.requiresAuth && !user) {
        navigate(APP_ROUTES.login);
        return;
      }
      if (link.route) {
        navigate(link.route);
      }
    },
    [navigate, onComingSoon, user],
  );

  return (
    <CommerceSidebarFrame
      icon="business_center"
      title="2Hands Commerce"
      subtitle="Khám phá sản phẩm"
    >
      {links.map((link) => {
        const active = isBuyerLinkActive(link, pathname);

        return (
          <button
            key={link.id}
            type="button"
            onClick={() => handleLinkClick(link)}
            className={[
              "flex items-center gap-3 rounded-lg px-4 py-3 text-left text-label-md transition-colors",
              active
                ? "bg-primary text-on-primary"
                : "text-on-surface-variant hover:bg-surface-container-high",
            ].join(" ")}
          >
            <span
              ref={link.id === "cart" ? setSidebarCartTargetRef : undefined}
              className="material-symbols-outlined"
              style={
                (link.id === "cart" || link.id === "orders") && active
                  ? { fontVariationSettings: "'FILL' 1" }
                  : undefined
              }
              aria-hidden="true"
            >
              {link.icon}
            </span>
            <span className="min-w-0 flex-1 truncate">{link.label}</span>
            {link.id === "cart" && user ? (
              <CartBadgePill
                count={cartItemCount}
                active={active}
                pulseToken={pulseToken}
                className="ml-auto"
              />
            ) : null}
          </button>
        );
      })}
    </CommerceSidebarFrame>
  );
}
