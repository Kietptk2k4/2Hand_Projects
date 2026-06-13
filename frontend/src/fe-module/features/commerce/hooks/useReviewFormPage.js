import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useSearchParams } from "react-router-dom";
import { fetchReviewContext, fetchReviewForEdit } from "../api/productReviewWriteApi";
import { mapProductReviewApiError } from "../constants/productReviewFormConstants";
import {
  mapReviewContextResponse,
  mapReviewForEditResponse,
} from "../utils/productReviewWriteMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function mapNavStateItem(state) {
  if (!state?.orderItem) return null;

  const item = state.orderItem;
  return {
    orderItemId: item.orderItemId,
    orderId: state.orderId,
    productId: item.productId,
    status: item.status,
    productNameSnapshot: item.productNameSnapshot,
    imageSnapshot: item.imageSnapshot,
    shopNameSnapshot: item.shopNameSnapshot,
    finalPrice: item.finalPrice,
    completedAt: item.completedAt,
    hasReview: Boolean(item.reviewId),
    reviewId: item.reviewId,
  };
}

export function useReviewFormPage({ mode, reviewId }) {
  const { showSessionExpired } = useAuthSession();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const orderItemId = searchParams.get("orderItemId");

  const [context, setContext] = useState(null);
  const [initialRating, setInitialRating] = useState(0);
  const [initialComment, setInitialComment] = useState("");
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const loadCreate = useCallback(async () => {
    const fromState = mapNavStateItem(location.state);
    if (fromState) {
      if (fromState.hasReview && fromState.reviewId) {
        setStatus("error");
        setErrorMessage("Sản phẩm này đã có đánh giá.");
        return;
      }
      setContext(fromState);
      setStatus("ready");
      return;
    }

    if (!orderItemId) {
      setStatus("error");
      setErrorMessage("Thiếu thông tin sản phẩm cần đánh giá.");
      return;
    }

    setStatus("loading");
    try {
      const raw = await fetchReviewContext(orderItemId);
      const mapped = mapReviewContextResponse(raw);
      if (mapped.hasReview) {
        setStatus("error");
        setErrorMessage("Sản phẩm này đã có đánh giá.");
        return;
      }
      if (mapped.status !== "COMPLETED") {
        setStatus("error");
        setErrorMessage("Chỉ có thể đánh giá khi đơn hàng đã hoàn thành.");
        return;
      }
      setContext(mapped);
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(mapProductReviewApiError(error));
    }
  }, [location.state, orderItemId, showSessionExpired]);

  const loadEdit = useCallback(async () => {
    if (!reviewId) {
      setStatus("error");
      setErrorMessage("Không tìm thấy đánh giá.");
      return;
    }

    setStatus("loading");
    try {
      const raw = await fetchReviewForEdit(reviewId);
      const mapped = mapReviewForEditResponse(raw);
      setContext(mapped);
      setInitialRating(mapped.rating);
      setInitialComment(mapped.comment || "");
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(mapProductReviewApiError(error));
    }
  }, [reviewId, showSessionExpired]);

  useEffect(() => {
    if (mode === "create") {
      loadCreate();
    } else {
      loadEdit();
    }
  }, [mode, loadCreate, loadEdit]);

  const summary = useMemo(
    () =>
      context
        ? {
            imageUrl: context.imageSnapshot,
            productName: context.productNameSnapshot,
            shopName: context.shopNameSnapshot,
            price: context.finalPrice,
            completedAt: context.completedAt,
            productId: context.productId,
            orderId: context.orderId,
          }
        : null,
    [context],
  );

  return {
    context,
    summary,
    initialRating,
    initialComment,
    orderItemId: context?.orderItemId || orderItemId,
    productId: context?.productId,
    orderId: context?.orderId,
    reviewStatus: mode === "create" ? "VISIBLE" : (context?.status ?? "VISIBLE"),
    existingMediaCount: context?.mediaCount ?? 0,
    existingMedia: context?.media ?? [],
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    retry: mode === "create" ? loadCreate : loadEdit,
  };
}
