import { useCallback, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductDetailActionCard } from "../components/ProductDetailActionCard";
import { ProductDetailDescription } from "../components/ProductDetailDescription";
import { ProductDetailReviewsPreview } from "../components/ProductDetailReviewsPreview";
import { ProductDetailInfo } from "../components/ProductDetailInfo";
import { ProductDetailShopCard } from "../components/ProductDetailShopCard";
import { ProductDetailSkeleton } from "../components/ProductDetailSkeleton";
import { ProductMediaGallery } from "../components/ProductMediaGallery";
import { ShopVacationBanner } from "../components/ShopVacationBanner";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useProductDetail } from "../hooks/useProductDetail";
import { useProductReviewsPreview } from "../hooks/useProductReviewsPreview";
import {
  buildCommerceCategoryPath,
  buildCommerceProductReviewsPath,
  buildCommerceShopPath,
  buildCommerceShopReviewsPath,
} from "../utils/commerceRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceProductDetailPage() {
  const { productId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const [toastMessage, setToastMessage] = useState("");
  const attributionFrom = searchParams.get("from");
  const attributionRequestId = searchParams.get("request_id");
  const { product, isLoading, isNotFound, isError, errorMessage, retry } = useProductDetail(
    productId,
    {
      from: attributionFrom,
      requestId: attributionRequestId,
    },
  );

  const {
    shop: previewShop,
    reviews: previewReviews,
    ratingSummary: previewRatingSummary,
    isLoading: isReviewsPreviewLoading,
    isEmpty: isReviewsPreviewEmpty,
    hasMoreReviews,
    errorMessage: reviewsPreviewError,
    retry: retryReviewsPreview,
  } = useProductReviewsPreview(productId);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const { addToCart, isAddingToCart } = useCommerceAddToCart({
    onSuccess: (message) => setToastMessage(message),
    onError: (message) => setToastMessage(message),
  });

  const { buyNow, isBuyingProduct } = useCommerceBuyNow({
    onError: (message) => setToastMessage(message),
  });

  const productFlyImageUrl =
    product?.media?.find((item) => item.mediaType === "IMAGE")?.mediaUrl || null;

  const onAddToCart = useCallback(
    (flyOptions = {}) => {
      if (!product?.productId) return;
      addToCart(product.productId, 1, {
        imageUrl: flyOptions.imageUrl || productFlyImageUrl,
        sourceElement: flyOptions.sourceElement,
        fromRect: flyOptions.fromRect,
      });
    },
    [addToCart, product?.productId, productFlyImageUrl]
  );

  const onBuyNow = useCallback(() => {
    if (!product?.productId) return;
    buyNow(product.productId, 1);
  }, [buyNow, product?.productId]);

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

  const openReviews = useCallback(() => {
    if (!productId) return;
    navigate(buildCommerceProductReviewsPath(productId));
  }, [navigate, productId]);

  const viewAllShopReviews = useCallback(
    (shopId) => {
      if (!shopId) return;
      navigate(buildCommerceShopReviewsPath(shopId));
    },
    [navigate]
  );

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        {/* Skeleton loader */}
        {isLoading ? <ProductDetailSkeleton /> : null}

        {/* Not Found */}
        {isNotFound ? (
          <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
            <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
              inventory_2
            </span>
            <p className="text-sm font-semibold text-on-surface-variant">
              {errorMessage || "Sản phẩm không tồn tại."}
            </p>
            <Link
              to={APP_ROUTES.commerceHome}
              className="mt-4 inline-block rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Về trang Commerce
            </Link>
          </div>
        ) : null}

        {/* Error */}
        {isError && !isNotFound ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {/* Main Product Showcase */}
        {!isLoading && !isNotFound && !isError && product ? (
          <>
            {/* Breadcrumb Navigation */}
            <nav
              className="mb-6 flex flex-wrap items-center text-xs font-semibold text-on-surface-variant"
              aria-label="Breadcrumb"
            >
              <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
                Trang chủ
              </Link>
              <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
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
                  <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
                    chevron_right
                  </span>
                </>
              ) : null}
              <span className="line-clamp-1 font-bold text-on-surface">{product.title}</span>
            </nav>

            {/* Shop Vacation Alert Banner */}
            {product.shopVacation ? (
              <ShopVacationBanner
                message={product.vacationMessage || "Shop đang nghỉ — không thể đặt hàng lúc này."}
              />
            ) : null}

            {/* Top Grid: Gallery (Left) & Info + Buy Actions (Right) */}
            <div className="grid grid-cols-1 gap-8 lg:grid-cols-12 lg:gap-10 mb-8">
              {/* Media Gallery */}
              <div className="lg:col-span-5">
                <ProductMediaGallery product={product} />
              </div>

              {/* Product Info, Buy Actions & Shop Card */}
              <div className="flex flex-col gap-5 lg:col-span-7">
                <ProductDetailInfo product={product} onOpenReviews={openReviews} />
                <ProductDetailActionCard
                  product={product}
                  productImageUrl={productFlyImageUrl}
                  onComingSoon={showComingSoon}
                  onAddToCart={onAddToCart}
                  onBuyNow={onBuyNow}
                  isAddingToCart={isAddingToCart}
                  isBuyingNow={isBuyingProduct(product.productId)}
                  currentUserId={user?.id}
                />
                <ProductDetailShopCard
                  product={product}
                  onVisitShop={visitShop}
                  onViewAllReviews={viewAllShopReviews}
                />
              </div>

              {/* Bottom Specs, Description & Buyer Reviews */}
              <div className="flex flex-col gap-8 lg:col-span-12">
                <ProductDetailDescription product={product} />
                <ProductDetailReviewsPreview
                  ratingSummary={previewRatingSummary}
                  reviews={previewReviews}
                  shop={previewShop}
                  isLoading={isReviewsPreviewLoading}
                  isEmpty={isReviewsPreviewEmpty}
                  hasMoreReviews={hasMoreReviews}
                  errorMessage={reviewsPreviewError}
                  onViewAll={openReviews}
                  onRetry={retryReviewsPreview}
                  onComingSoon={showComingSoon}
                />
              </div>
            </div>

            {/* E-Commerce Trust Badges Footer */}
            <CommerceFooterTrustSection />
          </>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
