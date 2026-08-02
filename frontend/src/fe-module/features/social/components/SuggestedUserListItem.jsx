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
      className="h-12 w-12 shrink-0 rounded-full object-cover"
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
          ? "flex items-center justify-between gap-4 border-b border-outline-variant/40 px-4 py-3.5 hover:bg-surface-container-low/50 transition-colors"
          : "flex items-center justify-between gap-3 px-4 py-3 hover:bg-surface-container-low/50 transition-colors"
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
          <span className="truncate text-[15px] font-bold leading-tight text-on-surface hover:underline">
            {item.name}
          </span>
          <span className="truncate text-[15px] text-on-surface-variant/70">
            {suggestionSubtitle(item.mutualFollowCount)}
          </span>
        </div>
      </button>
      <button
        type="button"
        disabled={followDisabled || isFollowLoading}
        onClick={() => onFollowToggle(item.userId, item.followStatus)}
        className={[
          "shrink-0 rounded-full px-4 py-1.5 text-[15px] font-bold transition-all shadow-2xs",
          isFollowing
            ? "border border-outline-variant/60 text-on-surface hover:bg-surface-container-high"
            : "bg-zinc-900 text-white hover:bg-zinc-800 active:scale-[0.97]",
          followDisabled || isFollowLoading ? "cursor-not-allowed opacity-60" : "",
        ].join(" ")}
      >
        {isFollowLoading ? "..." : followLabel}
      </button>
    </li>
  );
}
