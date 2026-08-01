import { useNavigate } from "react-router-dom";
import { buildCommerceShopPath } from "../utils/commerceRoutes";

export function CartShopGroupHeader({ shopId, shopName }) {
  const navigate = useNavigate();

  const handleOpenShop = (e) => {
    e.stopPropagation();
    if (shopId) {
      navigate(buildCommerceShopPath(shopId));
    }
  };

  return (
    <div className="flex items-center gap-2.5 rounded-t-2xl border-x border-t border-outline-variant/80 bg-surface-container-low px-4 py-3 border-b border-b-outline-variant/50">
      <span className="material-symbols-outlined text-lg text-primary" aria-hidden="true">
        storefront
      </span>
      <button
        type="button"
        onClick={handleOpenShop}
        className="flex items-center gap-1 text-xs font-bold text-on-surface hover:text-primary transition-colors cursor-pointer"
      >
        <span>{shopName || "Tủ đồ của Lan"}</span>
        <span className="material-symbols-outlined text-sm">chevron_right</span>
      </button>
      <span className="rounded-md bg-primary/10 px-2 py-0.5 text-[10px] font-bold text-primary">
        Shop đã xác minh
      </span>
    </div>
  );
}
