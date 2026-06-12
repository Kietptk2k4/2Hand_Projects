import { useCallback, useState } from "react";
import { createProductReview } from "../api/productReviewWriteApi";
import { mapProductReviewApiError } from "../constants/productReviewFormConstants";
import { mapCreateReviewResponse } from "../utils/productReviewWriteMapper";

export function useCreateProductReview() {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = useCallback(async ({ orderItemId, rating, comment }) => {
    setIsSubmitting(true);
    try {
      const raw = await createProductReview({ orderItemId, rating, comment });
      return mapCreateReviewResponse(raw);
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
