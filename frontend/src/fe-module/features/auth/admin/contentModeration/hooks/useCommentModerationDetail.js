import { useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getCommentForModeration } from "../api/socialModerationListApi.js";
import { mapCommentModerationDetailResponse } from "../utils/commentModerationDetailMapper.js";

export function useCommentModerationDetail(commentId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (!commentId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    let cancelled = false;

    (async () => {
      setStatus("loading");
      setErrorMessage("");

      try {
        const data = await getCommentForModeration(commentId);
        if (cancelled) return;
        setDetail(mapCommentModerationDetailResponse(data));
        setStatus("ready");
      } catch (error) {
        if (cancelled) return;
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        setStatus("error");
        setErrorMessage(error?.message || "Không tải được chi tiết bình luận.");
        setDetail(null);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [commentId, showSessionExpired]);

  return { detail, status, errorMessage };
}
