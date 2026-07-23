import { useCallback, useEffect, useState } from "react";
import { fetchAdminReviewDetail } from "../api/adminReviewModerationApi.js";
import { mapAdminReviewModerationApiError } from "../constants/adminReviewModerationConstants.js";
import { mapReviewModerationDetail } from "../utils/reviewModerationDetailMapper.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useReviewModerationDetail(reviewId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!reviewId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminReviewDetail(reviewId);
      setDetail(mapReviewModerationDetail(data));
      setStatus("ready");
    } catch (error) {
      const code = String(error?.code ?? "");
      if (code === "401" || code.includes("401") || code.includes("COMMERCE-401")) {
        showSessionExpired(error?.message);
        return;
      }
      setDetail(null);
      setStatus("error");
      setErrorMessage(mapAdminReviewModerationApiError(error));
    }
  }, [reviewId, showSessionExpired]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  return {
    detail,
    status,
    errorMessage,
    refetch: fetchDetail,
  };
}
