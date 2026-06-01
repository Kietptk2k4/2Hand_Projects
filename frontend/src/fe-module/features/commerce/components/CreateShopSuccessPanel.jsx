import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CreateShopSuccessPanel({ shopName, shopId }) {
  const storefrontPath = APP_ROUTES.commerceShopProducts.replace(":shopId", shopId);

  return (
    <div className="rounded-xl border border-primary/30 bg-surface-container-low p-8 text-center shadow-sm">
      <span
        className="material-symbols-outlined mb-3 text-5xl text-primary"
        style={{ fontVariationSettings: "'FILL' 1" }}
        aria-hidden="true"
      >
        check_circle
      </span>
      <h2 className="text-headline-md font-semibold text-on-surface">Tạo shop thành công</h2>
      <p className="mt-2 text-body-md text-on-surface-variant">
        Shop <strong className="text-on-surface">{shopName}</strong> đã sẵn sàng. Bạn có thể xem
        storefront và bắt đầu đăng sản phẩm sau.
      </p>
      <Link
        to={storefrontPath}
        className="mt-6 inline-flex items-center gap-1 rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Xem shop của tôi
        <span className="material-symbols-outlined text-lg" aria-hidden="true">
          storefront
        </span>
      </Link>
    </div>
  );
}
