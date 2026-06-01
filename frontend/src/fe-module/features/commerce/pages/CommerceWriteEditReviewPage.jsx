import { useCallback, useMemo, useState } from "react";
import { Link, useLocation, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductReviewForm } from "../components/ProductReviewForm";
import { ProductReviewFormSkeleton } from "../components/ProductReviewFormSkeleton";
import { ProductReviewSummaryAside } from "../components/ProductReviewSummaryAside";
import { useCreateProductReview } from "../hooks/useCreateProductReview";
import { useReviewFormPage } from "../hooks/useReviewFormPage";
import { useUpdateProductReview } from "../hooks/useUpdateProductReview";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceWriteEditReviewPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { reviewId: reviewIdParam } = useParams();
  const [searchParams] = useSearchParams();

  const mode = reviewIdParam ? "edit" : "create";
  const orderItemIdFromQuery = searchParams.get("orderItemId");

  const {
    summary,
    initialRating,
    initialComment,
    orderItemId,
    productId,
    orderId,
    isLoading,
    isError,
    errorMessage,
    retry,
  } = useReviewFormPage({ mode, reviewId: reviewIdParam });

  const { submit: submitCreate, isSubmitting: isCreating } = useCreateProductReview();
  const { submit: submitUpdate, isSubmitting: isUpdating } = useUpdateProductReview();

  const [apiError, setApiError] = useState("");
  const [toastMessage, setToastMessage] = useState("");

  const isSubmitting = isCreating || isUpdating;

  const returnTo = location.state?.returnTo;

  const backPath = useMemo(() => {
    if (returnTo) return returnTo;
    if (orderId) {
      return APP_ROUTES.commerceOrderDetail.replace(":orderId", orderId);
    }
    if (productId) {
      return APP_ROUTES.commerceProductReviews.replace(":productId", productId);
    }
    return APP_ROUTES.commerceOrders;
  }, [orderId, productId, returnTo]);

  const handleCancel = useCallback(() => {
    if (window.history.length > 1) {
      navigate(-1);
      return;
    }
    navigate(backPath);
  }, [backPath, navigate]);

  const handleSubmit = useCallback(
    async (payload) => {
      setApiError("");
      try {
        if (mode === "create") {
          const targetOrderItemId = orderItemId || orderItemIdFromQuery;
          if (!targetOrderItemId) {
            setApiError("Thiếu thông tin sản phẩm cần đánh giá.");
            return;
          }

          const result = await submitCreate({
            orderItemId: targetOrderItemId,
            rating: payload.rating,
            comment: payload.comment,
          });

          setToastMessage("Đánh giá của bạn đã được gửi thành công.");

          const reviewsPath = productId
            ? APP_ROUTES.commerceProductReviews.replace(":productId", productId)
            : backPath;

          setTimeout(() => {
            navigate(reviewsPath, { replace: true });
          }, 600);
          void result;
          return;
        }

        await submitUpdate(reviewIdParam, payload);
        setToastMessage("Đã cập nhật đánh giá.");
        setTimeout(() => {
          navigate(backPath, { replace: true });
        }, 600);
      } catch (error) {
        setApiError(error?.message || "Không thể lưu đánh giá. Vui lòng thử lại.");
      }
    },
    [
      backPath,
      mode,
      navigate,
      orderItemId,
      orderItemIdFromQuery,
      productId,
      reviewIdParam,
      submitCreate,
      submitUpdate,
    ],
  );

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        {isLoading ? <ProductReviewFormSkeleton /> : null}

        {isError ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <p className="text-sm text-on-surface-variant">{errorMessage}</p>
            <div className="mt-4 flex flex-wrap justify-center gap-3">
              <button
                type="button"
                onClick={retry}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
              >
                Thử lại
              </button>
              <Link
                to={backPath}
                className="rounded-lg border border-outline-variant px-4 py-2 text-sm text-on-surface hover:bg-surface-container-low"
              >
                Quay lại
              </Link>
            </div>
          </div>
        ) : null}

        {!isLoading && !isError && summary ? (
          <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
            <div className="lg:col-span-5">
              <ProductReviewSummaryAside
                imageUrl={summary.imageUrl}
                productName={summary.productName}
                shopName={summary.shopName}
                price={summary.price}
                completedAt={summary.completedAt}
              />
            </div>
            <div className="lg:col-span-7">
              <ProductReviewForm
                mode={mode}
                initialRating={initialRating}
                initialComment={initialComment}
                onSubmit={handleSubmit}
                onCancel={handleCancel}
                isSubmitting={isSubmitting}
                apiError={apiError}
              />
            </div>
          </div>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
