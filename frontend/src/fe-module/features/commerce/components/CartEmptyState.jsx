import { useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CartEmptyState() {
  const navigate = useNavigate();

  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-sm">
      <span className="material-symbols-outlined mb-4 text-5xl text-outline" aria-hidden="true">
        shopping_cart
      </span>
      <h2 className="text-headline-sm font-semibold text-on-surface">Giỏ hàng trống</h2>
      <p className="mt-2 text-sm text-on-surface-variant">
        Bạn chưa có sản phẩm nào. Khám phá marketplace và thêm vào giỏ nhé.
      </p>
      <button
        type="button"
        onClick={() => navigate(APP_ROUTES.commerceHome)}
        className="mt-6 rounded-lg bg-primary px-6 py-3 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Tiếp tục mua sắm
      </button>
    </div>
  );
}
