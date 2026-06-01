import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function SellerProductEmptyState() {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest py-16 text-center">
      <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
        inventory_2
      </span>
      <p className="text-body-md font-medium text-on-surface">Chưa có sản phẩm nào</p>
      <p className="mt-1 text-body-sm text-on-surface-variant">
        Thêm sản phẩm đầu tiên để bắt đầu bán trên 2Hands.
      </p>
      <Link
        to={APP_ROUTES.commerceSellerProductCreate}
        className="mt-6 inline-flex rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Thêm sản phẩm mới
      </Link>
    </div>
  );
}
