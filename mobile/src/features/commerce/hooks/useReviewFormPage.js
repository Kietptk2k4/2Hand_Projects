import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchReviewContext, fetchReviewForEdit } from "../api/productReviewWriteApi";
import { mapProductReviewApiError } from "../constants/productReviewFormConstants";
import {
  mapReviewContextResponse,
  mapReviewForEditResponse,
} from "../utils/productReviewWriteMapper";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useReviewFormPage({ mode, reviewId, orderItemId }) {
  const [context, setContext] = useState(null);
  const [initialRating, setInitialRating] = useState(0);
  const [initialComment, setInitialComment] = useState("");
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const loadCreate = useCallback(async () => {
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
        setStatus("error");
        setErrorMessage(mapProductReviewApiError(error));
        return;
      }
      setStatus("error");
      setErrorMessage(mapProductReviewApiError(error));
    }
  }, [orderItemId]);

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
        setStatus("error");
        setErrorMessage(mapProductReviewApiError(error));
        return;
      }
      setStatus("error");
      setErrorMessage(mapProductReviewApiError(error));
    }
  }, [reviewId]);

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
    reviewStatus: context?.status ?? "VISIBLE",
    existingMediaCount: context?.mediaCount ?? 0,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    retry: mode === "create" ? loadCreate : loadEdit,
  };
}
