import { useCallback, useState } from "react";
import { updateProductReview } from "../api/productReviewWriteApi";
import { mapProductReviewApiError } from "../constants/productReviewFormConstants";
import { mapUpdateReviewResponse } from "../utils/productReviewWriteMapper";

export function useUpdateProductReview() {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = useCallback(async (reviewId, patch) => {
    setIsSubmitting(true);
    try {
      const raw = await updateProductReview(reviewId, patch);
      return mapUpdateReviewResponse(raw);
    } catch (error) {
      throw {
        ...error,
        message: mapProductReviewApiError(error),
      };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  return { submit, isSubmitting };
}
