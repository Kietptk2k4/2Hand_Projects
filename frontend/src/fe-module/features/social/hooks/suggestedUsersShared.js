import { followUser, unfollowUser } from "../api/followApi";
import { fetchPublicUserProfile } from "../../auth/api/authApi";
import {
  isPlaceholderDisplayName,
  resolveSuggestedAvatarUrl,
  resolveSuggestedDisplayName,
} from "../utils/suggestedUserDisplay";

export function mapSuggestedUser(item) {
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

export async function enrichSuggestedUserFromAuth(item) {
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
      needsAvatar && profileAvatar ? profileAvatar : item.avatarUrl;

    return {
      ...item,
      name: nextName,
      avatarUrl: resolveSuggestedAvatarUrl(item.userId, nextAvatarUrl),
    };
  } catch {
    return item;
  }
}

export function suggestionSubtitle(mutualFollowCount) {
  if (mutualFollowCount > 0) {
    return `${mutualFollowCount} bạn chung`;
  }
  return "Gợi ý cho bạn";
}

export function followButtonLabel(followStatus) {
  switch (followStatus) {
    case "PENDING":
      return "Đã gửi";
    case "ACCEPTED":
      return "Đang theo dõi";
    default:
      return "Theo dõi";
  }
}

export async function mapAndEnrichSuggestedUsers(rawItems) {
  const mappedItems = (rawItems || []).map(mapSuggestedUser).filter((item) => item.userId);
  return Promise.all(mappedItems.map(enrichSuggestedUserFromAuth));
}

export function createFollowToggleHandler({
  setItems,
  setLoadingUserId,
  loadingUserId,
  isWriteBlocked,
  onToast,
  showSessionExpired,
  mapSocialWriteError,
}) {
  return async (userId, followStatus) => {
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
  };
}
