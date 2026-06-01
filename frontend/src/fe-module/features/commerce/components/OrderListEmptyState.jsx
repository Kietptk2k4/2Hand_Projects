import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function OrderListEmptyState() {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-10 text-center">
      <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
        receipt_long
      </span>
      <p className="text-headline-sm font-semibold text-on-surface">Bạn chưa có đơn hàng nào</p>
      <p className="mt-2 text-sm text-on-surface-variant">
        Hãy khám phá sản phẩm và đặt hàng để theo dõi tại đây.
      </p>
      <Link
        to={APP_ROUTES.commerceHome}
        className="mt-6 inline-block rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Tiếp tục mua sắm
      </Link>
    </div>
  );
}
