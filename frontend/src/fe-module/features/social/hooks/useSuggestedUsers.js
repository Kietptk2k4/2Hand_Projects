import { useCallback, useEffect, useState } from "react";
import { fetchSuggestedUsers } from "../api/discoveryApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { SUGGESTED_USERS_SIDEBAR_LIMIT } from "../constants/suggestedUsersConstants";
import {
  createFollowToggleHandler,
  followButtonLabel,
  mapAndEnrichSuggestedUsers,
  suggestionSubtitle,
} from "./suggestedUsersShared";

export function useSuggestedUsers({ onToast } = {}) {
  const { showSessionExpired } = useAuthSession();
  const { isWriteBlocked } = useSocialWriteBlock();
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [loadingUserId, setLoadingUserId] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSuggestedUsers({ limit: SUGGESTED_USERS_SIDEBAR_LIMIT });
      const enrichedItems = await mapAndEnrichSuggestedUsers(data?.items ?? []);
      setItems(enrichedItems);
      setMeta(data?.meta ?? null);
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
      }
      setItems([]);
      setMeta(null);
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được gợi ý người dùng.");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

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

  const hasMore =
    Boolean(meta?.hasNext) ||
    (typeof meta?.totalElements === "number" && meta.totalElements > items.length);

  return {
    items,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    hasMore,
    reload: load,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled: isWriteBlocked,
  };
}
