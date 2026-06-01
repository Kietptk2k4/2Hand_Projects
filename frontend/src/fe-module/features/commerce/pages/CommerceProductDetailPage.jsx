import { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductDetailActionCard } from "../components/ProductDetailActionCard";
import { ProductDetailDescription } from "../components/ProductDetailDescription";
import { ProductDetailInfo } from "../components/ProductDetailInfo";
import { ProductDetailShopCard } from "../components/ProductDetailShopCard";
import { ProductDetailSkeleton } from "../components/ProductDetailSkeleton";
import { ProductMediaGallery } from "../components/ProductMediaGallery";
import { ShopVacationBanner } from "../components/ShopVacationBanner";
import { useProductDetail } from "../hooks/useProductDetail";
import { buildCommerceCategoryPath, buildCommerceShopPath } from "../utils/commerceRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const { product, isLoading, isNotFound, isError, errorMessage, retry } =
    useProductDetail(productId);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const visitShop = useCallback(
    (shopId) => {
      if (!shopId) return;
      navigate(buildCommerceShopPath(shopId));
    },
    [navigate]
  );

  return (
    <CommerceShell onComingSoon={showComingSoon} showHomeSidebar={false}>
      <div className="mx-auto w-full max-w-[1280px]">
        {isLoading ? <ProductDetailSkeleton /> : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              inventory_2
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Sản phẩm không tồn tại."}
            </p>
            <Link
              to={APP_ROUTES.commerceHome}
              className="mt-4 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Về trang Commerce
            </Link>
          </div>
        ) : null}

        {isError && !isNotFound ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isLoading && !isNotFound && !isError && product ? (
          <>
            <nav
              className="mb-6 flex flex-wrap items-center text-body-sm text-on-surface-variant"
              aria-label="Breadcrumb"
            >
              <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
                Trang chủ
              </Link>
              <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
                chevron_right
              </span>
              {product.category?.categoryId ? (
                <>
                  <Link
                    to={buildCommerceCategoryPath(product.category.categoryId)}
                    className="transition-colors hover:text-primary"
                  >
                    {product.category.name}
                  </Link>
                  <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
                    chevron_right
                  </span>
                </>
              ) : null}
              <span className="line-clamp-1 font-medium text-on-surface">{product.title}</span>
            </nav>

            {product.shopVacation ? (
              <ShopVacationBanner
                message={product.vacationMessage || "Shop đang nghỉ — không thể đặt hàng lúc này."}
              />
            ) : null}

            <ProductMediaGallery product={product} />

            <div className="grid grid-cols-1 gap-8 lg:grid-cols-12 lg:gap-10">
              <div className="flex flex-col gap-8 lg:col-span-8">
                <ProductDetailInfo product={product} />
                <ProductDetailDescription product={product} />
              </div>

              <div className="lg:col-span-4">
                <ProductDetailActionCard product={product} onComingSoon={showComingSoon} />
                <ProductDetailShopCard
                  product={product}
                  onVisitShop={visitShop}
                  onComingSoon={showComingSoon}
                />
              </div>
            </div>
          </>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
