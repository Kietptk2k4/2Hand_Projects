import { useCallback, useEffect, useState } from "react";
import { fetchPostDetail } from "../api/postApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function usePostDetail(postId) {
  const { showSessionExpired } = useAuthSession();
  const [post, setPost] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);

  const load = useCallback(async () => {
    if (!postId) {
      setPost(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    setErrorCode(null);
    setPost(null);

    try {
      const data = await fetchPostDetail(postId);
      setPost(data);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorCode(error?.code || 500);
      setErrorMessage(error?.message || "Không tải được bài viết.");
    }
  }, [postId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    post,
    status,
    errorMessage,
    errorCode,
    isLoading: status === "loading",
    isError: status === "error",
    retry: load,
  };
}
