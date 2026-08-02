import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceHomeHero } from "../components/CommerceHomeHero";
import { CommerceFlashSaleSection } from "../components/CommerceFlashSaleSection";
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
import { useCommerceCategories } from "../hooks/useCommerceCategories";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useSellerShop } from "../context/SellerShopContext";
import { buildCommerceCategoryPath, buildCommerceShopPath } from "../utils/commerceRoutes";
import { buildCommerceSearchPath } from "../utils/commerceSearchRoutes";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";
import { MIN_KEYWORD_LENGTH } from "../constants/productSearchConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

const FEED_TABS = [
  { id: "recommend", label: "🔥 Gợi ý cho bạn" },
  { id: "flash", label: "⚡ Deal hời nhất" },
  { id: "newest", label: "✨ Hàng mới về" },
  { id: "bestseller", label: "🏆 Bán chạy" },
  { id: "vip", label: "💎 2Hand Select" },
];

export function CommerceHomePage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [activeTab, setActiveTab] = useState("recommend");
  const { user } = useAuthSession();
  const { isSeller } = useSellerShop();

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
        const el = document.getElementById("flash-sale-section");
        if (el) el.scrollIntoView({ behavior: "smooth" });
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
        const el = document.getElementById("flash-sale-section");
        if (el) el.scrollIntoView({ behavior: "smooth" });
      } else if (shortcutId === "2hand-select") {
        setActiveTab("vip");
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
    setActiveTab("flash");
    const el = document.getElementById("product-feed-section");
    if (el) {
      el.scrollIntoView({ behavior: "smooth" });
    }
  }, []);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      {/* Mobile Top Bell */}
      <div className="mb-4 flex items-center justify-end gap-2 lg:hidden">
        <NotificationBell buttonClassName="h-10 w-10 text-on-surface-variant hover:bg-surface-container-low hover:text-primary" />
      </div>

      {/* Hero Module (Banners, Shortcuts, Search, Create Shop, Shipping Exploration) */}
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

      {/* Flash Sale Section */}
      <div id="flash-sale-section">
        <CommerceFlashSaleSection
          products={items}
          isLoading={isInitialLoading}
          onOpenProduct={openProduct}
          onViewAll={handleViewAllFlashSales}
        />
      </div>

      {/* Featured Categories Showcase */}
      <div id="category-section">
        <CommerceCategoryGridSection
          categories={categories}
          isLoading={isLoadingCategories}
          onCategoryClick={navigateToCategory}
        />
      </div>

      {/* Main Product Catalog Section */}
      <section id="product-feed-section" className="mt-8">
        {/* Sticky Filter Header Bar */}
        <div className="sticky top-16 z-20 mb-6 flex items-center justify-between gap-4 rounded-2xl border border-outline-variant bg-surface-container-lowest/95 p-3 backdrop-blur-md shadow-xs sm:p-4">
          <h2 className="text-sm font-black uppercase text-on-surface sm:text-base flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">grid_view</span>
            DÀNH CHO BẠN
          </h2>

          {/* Sort Dropdown */}
          <div className="shrink-0">
            <ProductListSortSelect value={sort} onChange={changeSort} disabled={isInitialLoading} />
          </div>
        </div>

        {/* Loading Skeletons */}
        {isInitialLoading ? <ProductListSkeleton /> : null}

        {/* Error State */}
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

        {/* Empty State */}
        {!isInitialLoading && !errorMessage && items.length === 0 ? (
          <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
            <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
              inventory_2
            </span>
            <p className="text-sm font-semibold text-on-surface-variant">Chưa có sản phẩm nào để hiển thị.</p>
          </div>
        ) : null}

        {/* Product Cards Grid */}
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

        {/* Load More Button */}
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

      {/* Trust & Guarantee Section */}
      <div id="trust-shipping-section">
        <CommerceFooterTrustSection />
      </div>

      {/* Toast Notifications */}
      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
