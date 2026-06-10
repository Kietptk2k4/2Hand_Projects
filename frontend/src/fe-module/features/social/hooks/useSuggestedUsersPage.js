import { useCallback, useEffect, useRef, useState } from "react";
import { fetchSuggestedUsers } from "../api/discoveryApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { SUGGESTED_USERS_PAGE_SIZE } from "../constants/suggestedUsersConstants";
import {
  createFollowToggleHandler,
  followButtonLabel,
  mapAndEnrichSuggestedUsers,
  suggestionSubtitle,
} from "./suggestedUsersShared";

export function useSuggestedUsersPage({ onToast } = {}) {
  const { showSessionExpired } = useAuthSession();
  const { isWriteBlocked } = useSocialWriteBlock();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [loadingUserId, setLoadingUserId] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const data = await fetchSuggestedUsers({
          page: targetPage,
          limit: SUGGESTED_USERS_PAGE_SIZE,
        });
        if (requestId !== requestIdRef.current) return;

        const enrichedItems = await mapAndEnrichSuggestedUsers(data?.items ?? []);
        setItems((prev) => (append ? [...prev, ...enrichedItems] : enrichedItems));
        setMeta(data?.meta ?? null);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (String(error?.code ?? "").includes("401")) {
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được gợi ý người dùng.");
      }
    },
    [showSessionExpired]
  );

  useEffect(() => {
    loadPage(0, { append: false });
  }, [loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    if (items.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    loadPage(0, { append: false });
  }, [items.length, loadPage, page]);

  const handleFollowToggle = useCallback(
    createFollowToggleHandler({
      setItems,
      setLoadingUserId,
      loadingUserId,
      isWriteBlocked,
      onToast,
      showSessionExpired,
      mapSocialWriteError,
    }),
    [isWriteBlocked, loadingUserId, onToast, showSessionExpired]
  );

  return {
    items,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    isError: status === "error",
    errorMessage,
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    retry,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled: isWriteBlocked,
  };
}
