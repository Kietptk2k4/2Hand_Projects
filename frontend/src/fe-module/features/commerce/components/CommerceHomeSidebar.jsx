import { useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { APP_ROUTES } from "../../../shared/constants/routes";

const SIDEBAR_LINKS = [
  { id: "home", icon: "home", label: "Trang chủ", route: APP_ROUTES.commerceHome },
  { id: "cart", icon: "shopping_cart", label: "Giỏ hàng", route: APP_ROUTES.commerceCart, requiresAuth: true },
  { id: "marketplace", icon: "storefront", label: "Marketplace", comingSoon: true },
  {
    id: "orders",
    icon: "receipt_long",
    label: "Đơn hàng",
    route: APP_ROUTES.commerceOrders,
    requiresAuth: true,
  },
  {
    id: "create-shop",
    icon: "store",
    label: "Tạo shop",
    route: APP_ROUTES.commerceCreateShop,
    requiresAuth: true,
  },
  {
    id: "seller-products",
    icon: "inventory_2",
    label: "Sản phẩm",
    route: APP_ROUTES.commerceSellerProducts,
    requiresAuth: true,
  },
  {
    id: "seller-orders",
    icon: "shopping_bag",
    label: "Đơn seller",
    route: APP_ROUTES.commerceSellerOrders,
    requiresAuth: true,
  },
  {
    id: "seller-shipments",
    icon: "local_shipping",
    label: "Vận chuyển",
    route: APP_ROUTES.commerceSellerShipments,
    requiresAuth: true,
  },
  { id: "analytics", icon: "analytics", label: "Thống kê", comingSoon: true },
  {
    id: "settings",
    icon: "settings",
    label: "Cài đặt",
    route: APP_ROUTES.commerceShopSettings,
    requiresAuth: true,
  },
];

function isLinkActive(link, pathname) {
  if (!link.route) return false;
  if (link.id === "home") {
    return pathname === APP_ROUTES.commerceHome;
  }
  if (link.id === "cart") {
    return pathname === APP_ROUTES.commerceCart;
  }
  if (link.id === "orders") {
    return (
      pathname === APP_ROUTES.commerceOrders ||
      pathname.startsWith(`${APP_ROUTES.commerceOrders}/`)
    );
  }
  if (link.id === "create-shop") {
    return pathname === APP_ROUTES.commerceCreateShop;
  }
  if (link.id === "settings") {
    return (
      pathname === APP_ROUTES.commerceShopSettings ||
      pathname.startsWith(`${APP_ROUTES.commerceShopSettings}/`)
    );
  }
  if (link.id === "seller-products") {
    return (
      pathname === APP_ROUTES.commerceSellerProducts ||
      pathname.startsWith(`${APP_ROUTES.commerceSellerProducts}/`)
    );
  }
  if (link.id === "seller-orders") {
    return (
      pathname === APP_ROUTES.commerceSellerOrders ||
      pathname.startsWith(`${APP_ROUTES.commerceSellerOrders}/`)
    );
  }
  if (link.id === "seller-shipments") {
    return (
      pathname === APP_ROUTES.commerceSellerShipments ||
      pathname.startsWith(`${APP_ROUTES.commerceSellerShipments}/`)
    );
  }
  return pathname === link.route || pathname.startsWith(`${link.route}/`);
}

export function CommerceHomeSidebar({ onComingSoon }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { user } = useAuthSession();

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
    [navigate, onComingSoon, user]
  );

  return (
    <aside className="flex h-full w-64 flex-col overflow-y-auto overscroll-contain border-r border-outline-variant bg-surface-container-lowest px-3 py-6">
      <div className="mb-8 px-3">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-lg bg-surface-container-high">
            <span className="material-symbols-outlined text-primary" aria-hidden="true">
              business_center
            </span>
          </div>
          <div>
            <h2 className="text-headline-sm font-bold text-primary">2Hands Commerce</h2>
            <p className="text-label-sm text-on-surface-variant">Khám phá sản phẩm</p>
          </div>
        </div>
      </div>

      <nav className="flex flex-1 flex-col gap-1">
        {SIDEBAR_LINKS.map((link) => {
          const active = isLinkActive(link, pathname);

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
              {link.label}
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
