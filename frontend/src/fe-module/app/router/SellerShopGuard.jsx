import { Navigate, Outlet } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { useSellerShop } from "../../features/commerce/context/SellerShopContext";

function GuardLoading() {
  return (
    <div className="flex min-h-[40vh] items-center justify-center px-4">
      <p className="text-sm text-on-surface-variant">Đang tải thông tin shop...</p>
    </div>
  );
}

export function SellerShopGuard() {
  const { isSeller, isLoading } = useSellerShop();

  if (isLoading) {
    return <GuardLoading />;
  }

  if (!isSeller) {
    return (
      <Navigate
        to={APP_ROUTES.commerceCreateShop}
        replace
        state={{ message: "Bạn chưa có cửa hàng. Hãy đăng ký bán hàng trước." }}
      />
    );
  }

  return <Outlet />;
}
