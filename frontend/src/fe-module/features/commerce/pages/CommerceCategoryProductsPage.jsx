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
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
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
      <div className="mx-auto w-full max-w-[1280px]">
        <div className="lg:grid lg:grid-cols-12 lg:gap-6">
          {/* Category Sidebar */}
          <CategoryProductsSidebar
            activeCategoryId={categoryId}
            categoryName={category?.categoryName}
            categoryItems={sidebarItems}
            isLoadingCategories={isLoadingCategories}
            includeChildren={includeChildren}
            onIncludeChildrenChange={changeIncludeChildren}
          />

          {/* Main Product Showcase */}
          <main className="lg:col-span-9">
            <CategoryProductsMobileNav
              activeCategoryId={categoryId}
              categoryItems={sidebarItems}
              isLoadingCategories={isLoadingCategories}
            />

            {isNotFound ? (
              <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
                <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
                  category
                </span>
                <p className="text-sm font-semibold text-on-surface-variant">
                  {errorMessage || "Danh mục không tồn tại."}
                </p>
                <Link
                  to={APP_ROUTES.commerceHome}
                  className="mt-4 inline-block rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
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
                      inventory_2
                    </span>
                    <p className="text-sm font-semibold text-on-surface-variant">
                      Chưa có sản phẩm nào trong danh mục này.
                    </p>
                  </div>
                ) : null}

                {!isInitialLoading && !errorMessage && items.length > 0 ? (
                  <div className="grid grid-cols-2 gap-3 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 sm:gap-4 lg:gap-5">
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
              </>
            )}
          </main>
        </div>

        {/* E-Commerce Trust Badges Footer */}
        <div className="mt-12">
          <CommerceFooterTrustSection />
        </div>
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
