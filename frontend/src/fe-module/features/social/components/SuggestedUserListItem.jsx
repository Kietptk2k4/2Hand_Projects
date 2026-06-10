import { useState } from "react";
import { resolveSuggestedAvatarUrl } from "../utils/suggestedUserDisplay";

function SuggestedUserAvatar({ userId, avatarUrl, displayName }) {
  const [hasError, setHasError] = useState(false);
  const resolvedUrl = hasError
    ? resolveSuggestedAvatarUrl(userId, "")
    : resolveSuggestedAvatarUrl(userId, avatarUrl);

  return (
    <img
      src={resolvedUrl}
      alt={displayName ? `Avatar ${displayName}` : ""}
      className="h-10 w-10 shrink-0 rounded-full object-cover"
      onError={() => setHasError(true)}
    />
  );
}

export function SuggestedUserListItem({
  item,
  onViewProfile,
  onFollowToggle,
  followButtonLabel,
  suggestionSubtitle,
  loadingUserId,
  followDisabled = false,
  variant = "sidebar",
}) {
  const isFollowLoading = loadingUserId === item.userId;
  const followLabel = followButtonLabel(item.followStatus);
  const isFollowing = item.followStatus === "ACCEPTED" || item.followStatus === "PENDING";

  return (
    <li
      className={
        variant === "page"
          ? "flex items-center justify-between gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm"
          : "flex items-center justify-between gap-2"
      }
    >
      <button
        type="button"
        onClick={() => onViewProfile?.(item.userId)}
        className="flex min-w-0 flex-1 items-center gap-3 text-left"
      >
        <SuggestedUserAvatar
          userId={item.userId}
          avatarUrl={item.avatarUrl}
          displayName={item.name}
        />
        <div className="min-w-0 flex flex-col">
          <span className="truncate text-sm font-medium text-on-surface hover:text-primary">
            {item.name}
          </span>
          <span className="truncate text-xs font-semibold text-on-surface-variant">
            {suggestionSubtitle(item.mutualFollowCount)}
          </span>
        </div>
      </button>
      <button
        type="button"
        disabled={followDisabled || isFollowLoading}
        onClick={() => onFollowToggle(item.userId, item.followStatus)}
        className={[
          "shrink-0 rounded-full border px-3 py-1 text-xs font-semibold transition-colors",
          isFollowing
            ? "border-outline-variant text-on-surface-variant hover:bg-surface-container-high"
            : "border-primary text-primary hover:bg-[#e7eeff]",
          followDisabled || isFollowLoading ? "cursor-not-allowed opacity-60" : "",
        ].join(" ")}
      >
        {isFollowLoading ? "..." : followLabel}
      </button>
    </li>
  );
}
