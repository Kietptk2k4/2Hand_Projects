import { useCallback, useEffect, useState } from "react";
import { fetchTrendingHashtags } from "../api/discoveryApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { formatSocialCount } from "../utils/formatSocialCount";

const SIDEBAR_LIMIT = 5;

function mapTrendingHashtag(item) {
  const postCount = Number(item?.post_count ?? item?.postCount ?? 0);
  return {
    tag: item?.tag ?? "",
    postCount: Number.isFinite(postCount) ? postCount : 0,
  };
}

export function formatTrendingPostCount(postCount) {
  const formatted = formatSocialCount(postCount);
  if (!formatted) {
    return "0 bài viết";
  }
  return `${formatted} bài viết`;
}

export function useTrendingHashtags({ limit = SIDEBAR_LIMIT } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchTrendingHashtags({ limit });
      const rawItems = data?.items ?? [];
      setItems(rawItems.map(mapTrendingHashtag).filter((item) => item.tag));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
      }
      setItems([]);
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được hashtag thịnh hành.");
    }
  }, [limit, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    items,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    reload: load,
    formatPostCount: formatTrendingPostCount,
  };
}