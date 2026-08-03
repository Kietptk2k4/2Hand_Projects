import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { ProductListSortSelect } from "../components/ProductListSortSelect";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useFlashSaleProducts } from "../hooks/useFlashSaleProducts";
import { buildCommerceShopPath } from "../utils/commerceRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { PAGE_SIZE } from "../constants/productListConstants";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceFlashSalePage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");

  const {
    items,
    sort,
    changeSort,
    slotEnd,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useFlashSaleProducts({ enabled: true, limit: PAGE_SIZE, paginated: true });

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
        <nav className="mb-4 flex flex-wrap items-center text-xs font-semibold text-on-surface-variant">
          <Link to={APP_ROUTES.commerceHome} className="hover:text-primary">
            Commerce
          </Link>
          <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
            chevron_right
          </span>
          <span className="font-bold text-on-surface">Flash Sale</span>
        </nav>

        <header className="mb-6 rounded-2xl border border-rose-200 bg-gradient-to-r from-rose-50 via-red-50 to-orange-50 p-5 shadow-xs sm:p-6">
          <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div>
              <h1 className="flex items-center gap-2 text-xl font-black uppercase italic tracking-wide text-red-600 sm:text-2xl">
                <span className="material-symbols-outlined text-2xl">local_fire_department</span>
                Flash Sale
              </h1>
              <p className="mt-2 text-xs font-semibold text-on-surface-variant sm:text-sm">
                Deal khuyến mãi đang diễn ra trong khung giờ hiện tại
                {slotEnd
                  ? ` · kết thúc ${new Date(slotEnd).toLocaleString("vi-VN")}`
                  : ""}
              </p>
            </div>
            <div className="shrink-0">
              <ProductListSortSelect
                value={sort}
                onChange={changeSort}
                disabled={isInitialLoading}
              />
            </div>
          </div>
        </header>

        {isInitialLoading ? <ProductListSkeleton /> : null}

        {!isInitialLoading && errorMessage ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb] cursor-pointer"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {!isInitialLoading && !errorMessage && items.length === 0 ? (
          <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
            <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
              bolt
            </span>
            <p className="text-sm font-semibold text-on-surface-variant">
              Hiện chưa có deal Flash Sale nào.
            </p>
          </div>
        ) : null}

        {!isInitialLoading && !errorMessage && items.length > 0 ? (
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 sm:gap-4 lg:gap-5">
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
              />
            ))}
          </div>
        ) : null}

        {!isInitialLoading && !errorMessage && hasNext ? (
          <div className="mt-10 flex justify-center">
            {isLoadingMore ? (
              <div
                className="h-9 w-9 animate-spin rounded-full border-4 border-primary/20 border-t-primary"
                aria-label="Đang tải thêm"
              />
            ) : (
              <button
                type="button"
                onClick={loadMore}
                className="rounded-xl border-2 border-primary px-10 py-3 text-xs font-bold text-primary transition-all hover:bg-primary hover:text-on-primary shadow-xs active:scale-95 cursor-pointer"
              >
                Tải thêm sản phẩm
              </button>
            )}
          </div>
        ) : null}

        <div className="mt-12">
          <CommerceFooterTrustSection />
        </div>
      </div>

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
