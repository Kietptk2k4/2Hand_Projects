import { useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useSellerShop } from "../context/SellerShopContext";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { CommerceSidebarFrame } from "./CommerceSidebarFrame";

const SELLER_LINKS = [
  {
    id: "seller-products",
    icon: "inventory_2",
    label: "Sản phẩm",
    route: APP_ROUTES.commerceSellerProducts,
  },
  {
    id: "seller-orders",
    icon: "shopping_bag",
    label: "Đơn seller",
    route: APP_ROUTES.commerceSellerOrders,
  },
  {
    id: "seller-shipments",
    icon: "local_shipping",
    label: "Vận chuyển",
    route: APP_ROUTES.commerceSellerShipments,
  },
  {
    id: "seller-reviews",
    icon: "rate_review",
    label: "Đánh giá",
    route: APP_ROUTES.commerceSellerReviews,
  },
  {
    id: "settings",
    icon: "settings",
    label: "Cài đặt",
    route: APP_ROUTES.commerceShopSettings,
  },
];

function isSellerLinkActive(link, pathname) {
  if (!link.route) return false;
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
  if (link.id === "seller-reviews") {
    return (
      pathname === APP_ROUTES.commerceSellerReviews ||
      pathname.startsWith(`${APP_ROUTES.commerceSellerReviews}/`)
    );
  }
  return pathname === link.route || pathname.startsWith(`${link.route}/`);
}

export function CommerceSellerSidebar({ onComingSoon }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { shop } = useSellerShop();

  const handleLinkClick = useCallback(
    (link) => {
      if (link.comingSoon) {
        onComingSoon?.();
        return;
      }
      if (link.route) {
        navigate(link.route);
      }
    },
    [navigate, onComingSoon],
  );

  const subtitle = shop?.shopName ? `Shop · ${shop.shopName}` : "Quản lý bán hàng";

  return (
    <CommerceSidebarFrame icon="storefront" title="Khu bán hàng" subtitle={subtitle}>
      {SELLER_LINKS.map((link) => {
        const active = isSellerLinkActive(link, pathname);

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
            <span className="material-symbols-outlined" aria-hidden="true">
              {link.icon}
            </span>
            <span className="min-w-0 flex-1 truncate">{link.label}</span>
          </button>
        );
      })}

      <div className="mt-auto border-t border-outline-variant pt-4">
        <button
          type="button"
          onClick={() => navigate(APP_ROUTES.commerceHome)}
          className="flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left text-label-md text-on-surface-variant transition-colors hover:bg-surface-container-high"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            shopping_bag
          </span>
          <span className="min-w-0 flex-1 truncate">Về trang mua sắm</span>
        </button>
      </div>
    </CommerceSidebarFrame>
  );
}
