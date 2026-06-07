import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { formatFollowedAt } from "../utils/formatFollowedAt";

function initialsFromName(name) {
  const parts = (name || "U").trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "U";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

export function LikesListRow({ item, onViewProfile }) {
  const displayName = item.displayName || DEFAULT_USER_DISPLAY_NAME;
  const likedLabel = formatFollowedAt(item.likedAt);

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
          {likedLabel ? (
            <p className="text-xs text-on-surface-variant">Thích lúc {likedLabel}</p>
          ) : null}
        </div>
      </button>
    </div>
  );
}