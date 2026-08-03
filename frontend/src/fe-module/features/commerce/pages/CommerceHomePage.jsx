import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceHomeHero } from "../components/CommerceHomeHero";
import { CommerceFlashSaleSection } from "../components/CommerceFlashSaleSection";
import { CommerceNewestSection } from "../components/CommerceNewestSection";
import { CommerceCategoryGridSection } from "../components/CommerceCategoryGridSection";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { ProductListSortSelect } from "../components/ProductListSortSelect";
import { NotificationBell } from "../../notification/components/NotificationBell";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useProductList } from "../hooks/useProductList";
import { useHomeRecommendations } from "../hooks/useHomeRecommendations";
import { useFlashSaleProducts } from "../hooks/useFlashSaleProducts";
import { useNewestProducts } from "../hooks/useNewestProducts";
import { useCommerceCategories } from "../hooks/useCommerceCategories";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useSellerShop } from "../context/SellerShopContext";
import { buildCommerceCategoryPath, buildCommerceShopPath } from "../utils/commerceRoutes";
import { buildCommerceSearchPath } from "../utils/commerceSearchRoutes";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";
import { MIN_KEYWORD_LENGTH } from "../constants/productSearchConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";
const HOME_PREVIEW_LIMIT = 10;

export function CommerceHomePage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const { user } = useAuthSession();
  const { isSeller } = useSellerShop();

  const useHybridHome = Boolean(user);
  const catalog = useProductList({ enabled: !useHybridHome });
  const homeRecs = useHomeRecommendations({ enabled: useHybridHome });
  const flashSale = useFlashSaleProducts({ enabled: true, limit: HOME_PREVIEW_LIMIT });
  const newest = useNewestProducts({ enabled: true, limit: HOME_PREVIEW_LIMIT });

  const items = useHybridHome ? homeRecs.items : catalog.items;
  const sort = catalog.sort;
  const changeSort = catalog.changeSort;
  const isInitialLoading = useHybridHome ? homeRecs.isInitialLoading : catalog.isInitialLoading;
  const isLoadingMore = useHybridHome ? false : catalog.isLoadingMore;
  const hasNext = useHybridHome ? false : catalog.hasNext;
  const errorMessage = useHybridHome ? homeRecs.errorMessage : catalog.errorMessage;
  const loadMore = useHybridHome ? homeRecs.loadMore : catalog.loadMore;
  const retry = useHybridHome ? homeRecs.retry : catalog.retry;
  const homeRequestId = useHybridHome ? homeRecs.requestId : null;

  const {
    categories,
    homeNavItems,
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
      const base = APP_ROUTES.commerceProductDetail.replace(":productId", productId);
      if (useHybridHome && homeRequestId) {
        const params = new URLSearchParams({ from: "home", request_id: homeRequestId });
        navigate(`${base}?${params.toString()}`);
        return;
      }
      navigate(base);
    },
    [homeRequestId, navigate, useHybridHome]
  );

  const navigateToCategory = useCallback(
    (item) => {
      if (!item?.categoryId) return;
      navigate(buildCommerceCategoryPath(item.categoryId));
    },
    [navigate]
  );

  const handleCreateShopClick = useCallback(() => {
    if (!user) {
      navigate(APP_ROUTES.login);
      return;
    }
    if (isSeller) {
      navigate(APP_ROUTES.commerceSellerProducts);
    } else {
      navigate(APP_ROUTES.commerceCreateShop);
    }
  }, [isSeller, navigate, user]);

  const handleExploreShippingClick = useCallback(() => {
    const el = document.getElementById("trust-shipping-section");
    if (el) {
      el.scrollIntoView({ behavior: "smooth" });
    }
  }, []);

  const handleBannerCtaClick = useCallback(
    (bannerId) => {
      if (bannerId === 1) {
        navigate(APP_ROUTES.commerceFlashSale);
      } else if (bannerId === 2) {
        const vintageCat = categories.find((c) =>
          c.categoryName?.toLowerCase().includes("vintage")
        );
        if (vintageCat) {
          navigate(buildCommerceCategoryPath(vintageCat.categoryId));
        } else {
          const el = document.getElementById("category-section");
          if (el) el.scrollIntoView({ behavior: "smooth" });
        }
      } else if (bannerId === 3) {
        handleExploreShippingClick();
      }
    },
    [categories, handleExploreShippingClick, navigate]
  );

  const handleShortcutClick = useCallback(
    (shortcutId) => {
      if (shortcutId === "flash-sale") {
        navigate(APP_ROUTES.commerceFlashSale);
      } else if (shortcutId === "2hand-select") {
        const el = document.getElementById("product-feed-section");
        if (el) el.scrollIntoView({ behavior: "smooth" });
      } else if (shortcutId === "freeship" || shortcutId === "guarantee") {
        handleExploreShippingClick();
      } else if (shortcutId === "fashion-men") {
        const cat = categories.find((c) => c.categoryName?.toLowerCase().includes("nam"));
        if (cat) navigate(buildCommerceCategoryPath(cat.categoryId));
        else navigate(buildCommerceSearchPath("nam"));
      } else if (shortcutId === "fashion-women") {
        const cat = categories.find((c) => c.categoryName?.toLowerCase().includes("nu"));
        if (cat) navigate(buildCommerceCategoryPath(cat.categoryId));
        else navigate(buildCommerceSearchPath("nu"));
      } else if (shortcutId === "shoes") {
        const cat = categories.find((c) => c.categoryName?.toLowerCase().includes("giay"));
        if (cat) navigate(buildCommerceCategoryPath(cat.categoryId));
        else navigate(buildCommerceSearchPath("giay"));
      } else if (shortcutId === "tech") {
        navigate(buildCommerceSearchPath("dong ho"));
      }
    },
    [categories, handleExploreShippingClick, navigate]
  );

  const openShop = useCallback(
    (targetShopId) => {
      if (!targetShopId) return;
      navigate(buildCommerceShopPath(targetShopId));
    },
    [navigate]
  );

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

  const handleViewAllFlashSales = useCallback(() => {
    navigate(APP_ROUTES.commerceFlashSale);
  }, [navigate]);

  const handleViewAllNewest = useCallback(() => {
    navigate(APP_ROUTES.commerceNewest);
  }, [navigate]);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mb-4 flex items-center justify-end gap-2 lg:hidden">
        <NotificationBell buttonClassName="h-10 w-10 text-on-surface-variant hover:bg-surface-container-low hover:text-primary" />
      </div>

      <CommerceHomeHero
        onSearchSubmit={handleSearchSubmit}
        onCategoryClick={navigateToCategory}
        onCreateShopClick={handleCreateShopClick}
        onExploreShippingClick={handleExploreShippingClick}
        onBannerCtaClick={handleBannerCtaClick}
        onShortcutClick={handleShortcutClick}
        navItems={homeNavItems}
        isLoadingNav={isLoadingCategories}
      />

      <div id="flash-sale-section">
        <CommerceFlashSaleSection
          products={flashSale.items}
          isLoading={flashSale.isLoading}
          slotEnd={flashSale.slotEnd}
          onOpenProduct={openProduct}
          onViewAll={handleViewAllFlashSales}
        />
      </div>

      <CommerceNewestSection
        products={newest.items}
        isLoading={newest.isLoading}
        onOpenProduct={openProduct}
        onViewAll={handleViewAllNewest}
      />

      <div id="category-section">
        <CommerceCategoryGridSection
          categories={categories}
          isLoading={isLoadingCategories}
          onCategoryClick={navigateToCategory}
        />
      </div>

      <section id="product-feed-section" className="mt-8">
        <div className="sticky top-16 z-20 mb-6 flex items-center justify-between gap-4 rounded-2xl border border-outline-variant bg-surface-container-lowest/95 p-3 backdrop-blur-md shadow-xs sm:p-4">
          <div>
            <h2 className="text-sm font-black uppercase text-on-surface sm:text-base flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-xl">grid_view</span>
              DÀNH CHO BẠN
            </h2>
            {useHybridHome ? (
              <p className="mt-0.5 text-[11px] font-semibold text-on-surface-variant">
                Gợi ý cho bạn
              </p>
            ) : null}
          </div>

          {!useHybridHome ? (
            <div className="shrink-0">
              <ProductListSortSelect value={sort} onChange={changeSort} disabled={isInitialLoading} />
            </div>
          ) : null}
        </div>

        {isInitialLoading ? <ProductListSkeleton /> : null}

        {!isInitialLoading && errorMessage ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-medium text-on-error-container">{errorMessage}</p>
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
            <p className="text-sm font-semibold text-on-surface-variant">Chưa có sản phẩm nào để hiển thị.</p>
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
      </section>

      <div id="trust-shipping-section">
        <CommerceFooterTrustSection />
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
