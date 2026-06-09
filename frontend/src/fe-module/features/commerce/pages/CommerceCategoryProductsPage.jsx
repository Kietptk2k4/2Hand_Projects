import { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CategoryProductsHeader } from "../components/CategoryProductsHeader";
import {
  CategoryProductsMobileNav,
  CategoryProductsSidebar,
} from "../components/CategoryProductsSidebar";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useCategoryProducts } from "../hooks/useCategoryProducts";
import { useCommerceCategories } from "../hooks/useCommerceCategories";
import { buildCommerceShopPath } from "../utils/commerceRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceCategoryProductsPage() {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");

  const {
    items,
    category,
    pagination,
    sort,
    includeChildren,
    changeSort,
    changeIncludeChildren,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useCategoryProducts(categoryId);

  const {
    sidebarItems,
    isLoading: isLoadingCategories,
  } = useCommerceCategories();

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
      <div className="mx-auto w-full max-w-[1280px] lg:grid lg:grid-cols-12 lg:gap-6">
        <CategoryProductsSidebar
          activeCategoryId={categoryId}
          categoryName={category?.categoryName}
          categoryItems={sidebarItems}
          isLoadingCategories={isLoadingCategories}
          includeChildren={includeChildren}
          onIncludeChildrenChange={changeIncludeChildren}
        />

        <main className="lg:col-span-9">
          <CategoryProductsMobileNav
            activeCategoryId={categoryId}
            categoryItems={sidebarItems}
            isLoadingCategories={isLoadingCategories}
          />

          {isNotFound ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                category
              </span>
              <p className="text-sm text-on-surface-variant">
                {errorMessage || "Danh mục không tồn tại."}
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
              <CategoryProductsHeader
                categoryName={category?.categoryName || "..."}
                categorySlug={category?.categorySlug}
                totalItems={pagination?.totalItems}
                sort={sort}
                onSortChange={changeSort}
                includeChildren={includeChildren}
                onIncludeChildrenChange={changeIncludeChildren}
                sortDisabled={isInitialLoading}
              />

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
                  <p className="text-sm text-on-surface-variant">
                    Chưa có sản phẩm trong danh mục này.
                  </p>
                </div>
              ) : null}

              {!isInitialLoading && !errorMessage && items.length > 0 ? (
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
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
        </main>
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
