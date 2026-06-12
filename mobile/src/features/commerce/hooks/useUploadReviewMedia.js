import { useCallback, useState } from "react";
import { uploadReviewMedia } from "../api/productReviewWriteApi";
import { mapReviewMediaApiError } from "../constants/reviewMediaConstants";
import { mapUploadReviewMediaResponse } from "../utils/productReviewWriteMapper";

export function useUploadReviewMedia() {
  const [isUploading, setIsUploading] = useState(false);

  const upload = useCallback(async (reviewId, assets) => {
    if (!reviewId || !assets?.length) {
      return { media: [] };
    }

    setIsUploading(true);
    try {
      const raw = await uploadReviewMedia(reviewId, assets);
      return mapUploadReviewMediaResponse(raw);
    } catch (error) {
      throw {
        ...error,
        message: mapReviewMediaApiError(error),
      };
    } finally {
      setIsUploading(false);
    }
  }, []);

  return { upload, isUploading };
}
