import { useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getPostForModeration } from "../api/socialModerationListApi.js";
import { mapPostModerationDetailResponse } from "../utils/postModerationDetailMapper.js";

export function usePostModerationDetail(postId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (!postId) {
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
        const data = await getPostForModeration(postId);
        if (cancelled) return;
        setDetail(mapPostModerationDetailResponse(data));
        setStatus("ready");
      } catch (error) {
        if (cancelled) return;
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        setStatus("error");
        setErrorMessage(error?.message || "Không tải được chi tiết bài viết.");
        setDetail(null);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [postId, showSessionExpired]);

  return { detail, status, errorMessage };
}
