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
          label: "Khu bán hàng (Seller)",
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
      icon="shopping_bag"
      title="2Hands Commerce"
      subtitle="Chợ mua bán 2Hand"
    >
      {/* Return to Social Feed Button */}
      <button
        type="button"
        onClick={() => navigate(APP_ROUTES.socialFeed)}
        className="mb-3 flex w-full items-center gap-3 rounded-xl border border-blue-200/80 bg-blue-50/70 px-4 py-2.5 text-left text-xs font-bold text-blue-700 transition-all hover:bg-blue-100 hover:border-blue-300 shadow-2xs group cursor-pointer"
      >
        <span className="material-symbols-outlined text-lg text-blue-600 group-hover:-translate-x-0.5 transition-transform">
          arrow_back
        </span>
        <span className="min-w-0 flex-1 truncate font-extrabold">Về Mạng xã hội (Social)</span>
      </button>

      {links.map((link) => {
        const active = isBuyerLinkActive(link, pathname);

        return (
          <button
            key={link.id}
            type="button"
            onClick={() => handleLinkClick(link)}
            className={[
              "flex items-center gap-3 rounded-xl px-4 py-3 text-left text-xs font-bold transition-all",
              active
                ? "bg-primary text-on-primary shadow-xs"
                : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
            ].join(" ")}
          >
            <span
              ref={link.id === "cart" ? setSidebarCartTargetRef : undefined}
              className="material-symbols-outlined text-lg"
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

      {/* Mini Promotion Banner inside Sidebar */}
      <div className="mt-8 rounded-xl border border-amber-200/80 bg-gradient-to-br from-amber-50 to-orange-50 p-4 text-xs text-slate-800">
        <div className="flex items-center gap-1.5 font-bold text-amber-700">
          <span className="material-symbols-outlined text-base">verified</span>
          <span>Bảo Vệ Người Mua</span>
        </div>
        <p className="mt-1 text-[11px] text-slate-600 leading-relaxed">
          Thanh toán qua 2Hands - Hoàn tiền 100% nếu hàng không đúng mô tả.
        </p>
      </div>
    </CommerceSidebarFrame>
  );
}
