import { useCallback, useEffect, useState } from "react";
import { fetchMyProductReview } from "../api/productReviewWriteApi";
import { mapMyProductReviewResponse } from "../utils/productReviewWriteMapper";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useMyProductReview(productId) {
  const [myReview, setMyReview] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    if (!productId) {
      setMyReview(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchMyProductReview(productId);
      setMyReview(mapMyProductReviewResponse(raw));
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        setMyReview(null);
        setStatus("idle");
        setErrorMessage("");
        return;
      }
      setMyReview(null);
      setStatus("error");
      setErrorMessage(error?.message || "Không thể tải đánh giá của bạn.");
    }
  }, [productId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    myReview,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    refetch: load,
  };
}
