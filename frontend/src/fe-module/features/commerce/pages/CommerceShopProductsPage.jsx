import { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { ShopProductsHeader } from "../components/ShopProductsHeader";
import { ShopStorefrontHero } from "../components/ShopStorefrontHero";
import { ShopStorefrontHeroSkeleton } from "../components/ShopStorefrontHeroSkeleton";
import { ShopVacationBanner } from "../components/ShopVacationBanner";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useShopProducts } from "../hooks/useShopProducts";
import { buildCommerceShopPath } from "../utils/commerceRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceShopProductsPage() {
  const { shopId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const [toastMessage, setToastMessage] = useState("");

  const {
    items,
    shop,
    pagination,
    sort,
    changeSort,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useShopProducts(shopId);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const { addToCart, isAddingProduct } = useCommerceAddToCart({
    onSuccess: (message) => setToastMessage(message),
    onError: (message) => setToastMessage(message),
  });

  const { buyNow, isBuyingProduct } = useCommerceBuyNow({
    onError: (message) => setToastMessage(message),
  });

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const openProduct = useCallback(
    (productId) => {
      if (!productId) return;
      navigate(APP_ROUTES.commerceProductDetail.replace(":productId", productId));
    },
    [navigate]
  );

  const openShop = useCallback(
    (targetShopId) => {
      if (!targetShopId) return;
      navigate(buildCommerceShopPath(targetShopId));
    },
    [navigate]
  );

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
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
          <span className="font-medium text-on-surface">
            {shop?.shopName || "Shop"}
          </span>
        </nav>

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              storefront
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Shop không tồn tại hoặc không khả dụng."}
            </p>
            <Link
              to={APP_ROUTES.commerceHome}
              className="mt-4 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Về trang Commerce
            </Link>
          </div>
        ) : (
          <>
            {isInitialLoading ? <ShopStorefrontHeroSkeleton /> : null}
            {!isInitialLoading && shop ? (
              <ShopStorefrontHero shop={shop} onComingSoon={showComingSoon} />
            ) : null}

            {!isInitialLoading && shop?.shopVacation ? (
              <ShopVacationBanner
                message={shop.vacationMessage || "Shop đang nghỉ — đơn hàng sẽ được xử lý sau."}
              />
            ) : null}

            {!isInitialLoading && shop ? (
              <ShopProductsHeader
                totalItems={pagination?.totalItems}
                sort={sort}
                onSortChange={changeSort}
                sortDisabled={isInitialLoading}
              />
            ) : null}

            {isInitialLoading ? <ProductListSkeleton /> : null}

            {!isInitialLoading && errorMessage ? (
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

            {!isInitialLoading && !errorMessage && shop && items.length === 0 ? (
              <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
                <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                  inventory_2
                </span>
                <p className="text-sm text-on-surface-variant">
                  Shop chưa có sản phẩm nào để hiển thị.
                </p>
              </div>
            ) : null}

            {!isInitialLoading && !errorMessage && items.length > 0 ? (
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {items.map((product) => (
                  <ProductCard
                    key={product.productId}
                    product={product}
                    onOpenProduct={openProduct}
                    onOpenShop={openShop}
                    onAddToCart={addToCart}
                    onBuyNow={buyNow}
                    isAddingToCart={isAddingProduct(product.productId)}
                    isBuyingNow={isBuyingProduct(product.productId)}
                    disabledActions={
                      Boolean(shop?.shopVacation) ||
                      (Boolean(shop?.sellerId) &&
                        Boolean(user?.id) &&
                        String(shop.sellerId) === String(user.id))
                    }
                  />
                ))}
              </div>
            ) : null}

            {!isInitialLoading && !errorMessage && hasNext ? (
              <div className="mt-10 flex justify-center">
                {isLoadingMore ? (
                  <div
                    className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                    aria-label="Đang tải thêm"
                  />
                ) : (
                  <button
                    type="button"
                    onClick={loadMore}
                    className="rounded-md border-2 border-primary px-8 py-3 text-label-md font-bold text-primary transition-colors hover:bg-primary hover:text-on-primary"
                  >
                    Tải thêm sản phẩm
                  </button>
                )}
              </div>
            ) : null}
          </>
        )}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
