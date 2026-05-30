import { formatFollowedAt } from "../utils/formatFollowedAt";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function initialsFromName(name) {
  const parts = (name || "U").trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "U";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

export function FollowListRow({ item, onViewProfile }) {
  const displayName = item.displayName || "User";
  const followedLabel = formatFollowedAt(item.followedAt);

  return (
    <div className="group flex items-center justify-between border-b border-outline-variant/30 px-2 py-3 transition-colors hover:rounded-lg hover:bg-surface-container-low">
      <button
        type="button"
        onClick={() => onViewProfile?.(item.userId)}
        className="flex min-w-0 flex-1 items-center gap-3 text-left"
      >
        {item.avatarUrl ? (
          <img
            src={item.avatarUrl}
            alt=""
            className="h-10 w-10 shrink-0 rounded-full object-cover"
          />
        ) : (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-surface-container-high text-xs font-semibold text-on-surface-variant">
            {initialsFromName(displayName)}
          </div>
        )}
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold text-on-surface group-hover:text-primary">
            {displayName}
          </p>
          {followedLabel ? (
            <p className="text-xs text-on-surface-variant">Theo dõi từ {followedLabel}</p>
          ) : null}
        </div>
      </button>
    </div>
  );
}
