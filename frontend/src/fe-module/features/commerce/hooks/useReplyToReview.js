import { useCallback, useState } from "react";
import { postReplyToReview } from "../api/sellerShopReviewsApi";
import { mapSellerShopReviewsApiError } from "../constants/sellerShopReviewsConstants";
import { mapReplyToReviewResponse } from "../utils/sellerShopReviewsMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useReplyToReview({ onSuccess, onAlreadyReplied }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const reply = useCallback(
    async (reviewId, content) => {
      const trimmed = String(content ?? "").trim();
      if (!trimmed) {
        setSubmitError("Nội dung phản hồi không được để trống.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await postReplyToReview(reviewId, trimmed);
        const result = mapReplyToReviewResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }

        const code = String(error?.code ?? "");
        if (code === "COMMERCE-409-REVIEW-REPLY") {
          onAlreadyReplied?.(error);
          return null;
        }

        setSubmitError(mapSellerShopReviewsApiError(error));
        return null;
      } finally {
        setIsSubmitting(false);
      }
    },
    [onAlreadyReplied, onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setSubmitError(""), []);

  return {
    isSubmitting,
    submitError,
    reply,
    clearError,
  };
}
