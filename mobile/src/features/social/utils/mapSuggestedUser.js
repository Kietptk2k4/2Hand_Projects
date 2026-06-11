import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";

export function mapSuggestedUser(item) {
  const mutualFollowCount = Number(
    item?.mutual_follow_count ?? item?.mutualFollowCount ?? 0
  );

  return {
    userId: item?.user_id ?? item?.userId ?? "",
    displayName: item?.display_name ?? item?.displayName ?? DEFAULT_USER_DISPLAY_NAME,
    avatarUrl: item?.avatar_url ?? item?.avatarUrl ?? "",
    followStatus: item?.follow_status ?? item?.followStatus ?? "NONE",
    mutualFollowCount: Number.isFinite(mutualFollowCount) ? mutualFollowCount : 0,
  };
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
