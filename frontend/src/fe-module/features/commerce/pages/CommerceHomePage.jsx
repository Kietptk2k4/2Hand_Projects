import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceHomeHero } from "../components/CommerceHomeHero";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { ProductListSortSelect } from "../components/ProductListSortSelect";
import { CartBadgePill } from "../components/CartBadgePill";
import { useCartBadge } from "../context/CartBadgeContext";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useProductList } from "../hooks/useProductList";
import { buildCommerceCategoryPath, buildCommerceShopPath } from "../utils/commerceRoutes";
import { buildCommerceSearchPath } from "../utils/commerceSearchRoutes";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";
import { MIN_KEYWORD_LENGTH } from "../constants/productSearchConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceHomePage() {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const { itemCount: cartItemCount } = useCartBadge();
  const [toastMessage, setToastMessage] = useState("");
  const {
    items,
    sort,
    changeSort,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useProductList();

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const { addToCart, isAddingProduct } = useCommerceAddToCart({
    onSuccess: (message) => setToastMessage(message),
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

  const navigateToCategory = useCallback(
    (item) => {
      if (!item?.categoryId) return;
      navigate(buildCommerceCategoryPath(item.categoryId));
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

  const goToCart = useCallback(() => {
    if (user) {
      navigate(APP_ROUTES.commerceCart);
      return;
    }
    navigate(APP_ROUTES.login);
  }, [navigate, user]);

  const handleSearchSubmit = useCallback(
    (rawQuery) => {
      const normalized = normalizeSearchKeyword(rawQuery);
      if (!normalized) return;
      if (normalized.length < MIN_KEYWORD_LENGTH) {
        setToastMessage("Nhập ít nhất 2 ký tự.");
        return;
      }
      navigate(buildCommerceSearchPath(normalized));
    },
    [navigate]
  );

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mb-6 flex items-center justify-end gap-2 lg:hidden">
        <button
          type="button"
          onClick={showComingSoon}
          className="relative rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-primary"
          aria-label="Thông báo"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            notifications
          </span>
          <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-error" />
        </button>
        <button
          type="button"
          onClick={goToCart}
          className="relative rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-primary"
          aria-label="Giỏ hàng"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            shopping_cart
          </span>
          {user ? (
            <CartBadgePill
              count={cartItemCount}
              className="absolute -right-1 -top-1 min-h-4 min-w-4 px-1 text-[10px] leading-none"
            />
          ) : null}
        </button>
      </div>

      <CommerceHomeHero onSearchSubmit={handleSearchSubmit} onCategoryClick={navigateToCategory} />

      <section>
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="text-headline-md font-semibold text-on-surface">Sản phẩm nổi bật</h2>
          <ProductListSortSelect value={sort} onChange={changeSort} disabled={isInitialLoading} />
        </div>

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

        {!isInitialLoading && !errorMessage && items.length === 0 ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              inventory_2
            </span>
            <p className="text-sm text-on-surface-variant">Chưa có sản phẩm nào để hiển thị.</p>
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
                  onComingSoon={showComingSoon}
                  onAddToCart={addToCart}
                  isAddingToCart={isAddingProduct(product.productId)}
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
      </section>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
