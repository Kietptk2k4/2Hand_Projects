import { useCallback, useState } from "react";
import { uploadReviewMedia } from "../api/productReviewWriteApi";
import { mapReviewMediaApiError } from "../constants/reviewMediaConstants";
import { mapUploadReviewMediaResponse } from "../utils/productReviewWriteMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useUploadReviewMedia() {
  const { showSessionExpired } = useAuthSession();
  const [isUploading, setIsUploading] = useState(false);

  const upload = useCallback(
    async (reviewId, files) => {
      if (!reviewId || !files?.length) {
        return { media: [] };
      }

      setIsUploading(true);
      try {
        const raw = await uploadReviewMedia(reviewId, files);
        return mapUploadReviewMediaResponse(raw);
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        throw {
          ...error,
          message: mapReviewMediaApiError(error),
        };
      } finally {
        setIsUploading(false);
      }
    },
    [showSessionExpired],
  );

  return { upload, isUploading };
}
