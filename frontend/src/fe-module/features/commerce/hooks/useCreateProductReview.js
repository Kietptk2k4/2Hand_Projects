import { useCallback, useState } from "react";
import { createProductReview } from "../api/productReviewWriteApi";
import { mapProductReviewApiError } from "../constants/productReviewFormConstants";
import { mapCreateReviewResponse } from "../utils/productReviewWriteMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useCreateProductReview() {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = useCallback(
    async ({ orderItemId, rating, comment }) => {
      setIsSubmitting(true);
      try {
        const raw = await createProductReview({ orderItemId, rating, comment });
        return mapCreateReviewResponse(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        throw {
          ...error,
          message: mapProductReviewApiError(error),
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [showSessionExpired],
  );

  return { submit, isSubmitting };
}
