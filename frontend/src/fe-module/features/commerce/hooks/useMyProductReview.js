import { useCallback, useEffect, useState } from "react";
import { fetchMyProductReview } from "../api/productReviewWriteApi";
import { mapMyProductReviewResponse } from "../utils/productReviewWriteMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useMyProductReview(productId) {
  const { isAuthenticated, showSessionExpired } = useAuthSession();
  const [myReview, setMyReview] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    if (!productId || !isAuthenticated) {
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
        showSessionExpired(error?.message);
        return;
      }
      setMyReview(null);
      setStatus("error");
      setErrorMessage(error?.message || "Không thể tải đánh giá của bạn.");
    }
  }, [isAuthenticated, productId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    myReview,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    shouldShowStrip: isAuthenticated,
    refetch: load,
  };
}
