import { useCallback, useEffect, useState } from "react";
import { fetchSuggestedUsers } from "../api/discoveryApi";
import { followUser, unfollowUser } from "../api/followApi";
import { fetchPublicUserProfile } from "../../auth/api/authApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import {
  isPlaceholderDisplayName,
  resolveSuggestedAvatarUrl,
  resolveSuggestedDisplayName,
} from "../utils/suggestedUserDisplay";

const SIDEBAR_LIMIT = 3;
const EXPANDED_LIMIT = 20;

function mapSuggestedUser(item) {
  const mutualFollowCount = Number(
    item?.mutual_follow_count ?? item?.mutualFollowCount ?? 0
  );
  const userId = item?.user_id ?? item?.userId ?? "";
  const sourceDisplayName = item?.display_name ?? item?.displayName ?? "";
  const sourceAvatarUrl = item?.avatar_url ?? item?.avatarUrl ?? "";

  return {
    userId,
    sourceDisplayName,
    sourceAvatarUrl,
    name: resolveSuggestedDisplayName(sourceDisplayName, userId),
    avatarUrl: resolveSuggestedAvatarUrl(userId, sourceAvatarUrl),
    followStatus: item?.follow_status ?? item?.followStatus ?? "NONE",
    mutualFollowCount: Number.isFinite(mutualFollowCount) ? mutualFollowCount : 0,
  };
}

async function enrichSuggestedUserFromAuth(item) {
  const needsName = isPlaceholderDisplayName(item.sourceDisplayName, item.userId);
  const needsAvatar = !String(item.sourceAvatarUrl || "").trim();

  if (!needsName && !needsAvatar) {
    return item;
  }

  try {
    const profile = await fetchPublicUserProfile(item.userId);
    const profileName = profile?.display_name ?? profile?.displayName ?? "";
    const profileAvatar = profile?.avatar_url ?? profile?.avatarUrl ?? "";

    const nextName =
      needsName && profileName && !isPlaceholderDisplayName(profileName, item.userId)
        ? profileName
        : item.name;
    const nextAvatarUrl =
      needsAvatar && profileAvatar
        ? profileAvatar
        : item.avatarUrl;

    return {
      ...item,
      name: nextName,
      avatarUrl: resolveSuggestedAvatarUrl(item.userId, nextAvatarUrl),
    };
  } catch {
    return item;
  }
}

function suggestionSubtitle(mutualFollowCount) {
  if (mutualFollowCount > 0) {
    return `${mutualFollowCount} bạn chung`;
  }
  return "Gợi ý cho bạn";
}

function followButtonLabel(followStatus) {
  switch (followStatus) {
    case "PENDING":
      return "Đã gửi";
    case "ACCEPTED":
      return "Đang theo dõi";
    default:
      return "Theo dõi";
  }
}

export function useSuggestedUsers({ onToast } = {}) {
  const { showSessionExpired } = useAuthSession();
  const { isWriteBlocked } = useSocialWriteBlock();
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [expanded, setExpanded] = useState(false);
  const [loadingUserId, setLoadingUserId] = useState("");

  const currentLimit = expanded ? EXPANDED_LIMIT : SIDEBAR_LIMIT;

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSuggestedUsers({ limit: currentLimit });
      const rawItems = data?.items ?? [];
      const mappedItems = rawItems.map(mapSuggestedUser).filter((item) => item.userId);
      const enrichedItems = await Promise.all(mappedItems.map(enrichSuggestedUserFromAuth));
      setItems(enrichedItems);
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
      }
      setItems([]);
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được gợi ý người dùng.");
    }
  }, [currentLimit, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const expand = useCallback(() => {
    setExpanded(true);
  }, []);

  const handleFollowToggle = useCallback(
    async (userId, followStatus) => {
      if (!userId || isWriteBlocked || loadingUserId) return;
      if (followStatus === "SELF") return;

      setLoadingUserId(userId);

      try {
        if (followStatus === "NONE") {
          const data = await followUser(userId);
          const nextStatus = data?.status ?? "ACCEPTED";
          setItems((prev) =>
            prev.map((item) =>
              item.userId === userId ? { ...item, followStatus: nextStatus } : item
            )
          );
          onToast?.(
            nextStatus === "PENDING" ? "Đã gửi yêu cầu theo dõi." : "Đã theo dõi."
          );
        } else if (followStatus === "PENDING" || followStatus === "ACCEPTED") {
          await unfollowUser(userId);
          setItems((prev) =>
            prev.map((item) =>
              item.userId === userId ? { ...item, followStatus: "NONE" } : item
            )
          );
          onToast?.("Đã hủy theo dõi.");
        }
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return;
        }
        onToast?.(mapped.message);
      } finally {
        setLoadingUserId("");
      }
    },
    [isWriteBlocked, loadingUserId, onToast, showSessionExpired]
  );

  return {
    items,
    isLoading: status === "loading",
    isError: status === "error",
    errorMessage,
    expanded,
    expand,
    reload: load,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled: isWriteBlocked,
  };
}