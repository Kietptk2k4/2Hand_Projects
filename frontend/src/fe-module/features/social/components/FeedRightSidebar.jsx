import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSuggestedUsers } from "../hooks/useSuggestedUsers";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { resolveSuggestedAvatarUrl } from "../utils/suggestedUserDisplay";

const TRENDING = [
  { tag: "RemoteWork2024", count: "12.5k bài viết" },
  { tag: "AIinFinance", count: "8.2k bài viết" },
  { tag: "FreelanceTips", count: "5.1k bài viết" },
  { tag: "LegalTech", count: "3.9k bài viết" },
];

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

export function FeedRightSidebar({ onComingSoon, onViewProfile, onSelectHashtag, onToast }) {
  const navigate = useNavigate();
  const {
    items: suggestedItems,
    isLoading: isSuggestionsLoading,
    isError: isSuggestionsError,
    errorMessage: suggestionsErrorMessage,
    expanded,
    expand,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled,
  } = useSuggestedUsers({ onToast });

  const goToHashtag = (tag) => {
    const normalized = tag?.replace(/^#+/, "").trim();
    if (!normalized) return;
    if (onSelectHashtag) {
      onSelectHashtag(normalized);
      return;
    }
    navigate(buildSocialHashtagPath(normalized));
  };

  return (
    <aside className="hidden flex-col gap-6 lg:col-span-3 lg:flex lg:sticky lg:top-20 lg:max-h-[calc(100vh-5rem)] lg:self-start lg:overflow-y-auto">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Đang thịnh hành</h3>
        <ul className="flex flex-col gap-3">
          {TRENDING.map((item) => (
            <li key={item.tag} className="flex flex-col">
              <button
                type="button"
                className="text-left text-sm font-medium text-primary hover:underline"
                onClick={() => goToHashtag(item.tag)}
              >
                #{item.tag}
              </button>
              <span className="text-xs font-semibold text-on-surface-variant">{item.count}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Những người bạn có thể biết</h3>
        {isSuggestionsLoading ? (
          <p className="text-sm text-on-surface-variant">Đang tải...</p>
        ) : isSuggestionsError ? (
          <p className="text-sm text-error">{suggestionsErrorMessage}</p>
        ) : suggestedItems.length === 0 ? (
          <p className="text-sm text-on-surface-variant">Chưa có gợi ý người dùng.</p>
        ) : (
          <ul className="flex flex-col gap-4">
            {suggestedItems.map((item) => {
              const isFollowLoading = loadingUserId === item.userId;
              const followLabel = followButtonLabel(item.followStatus);
              const isFollowing = item.followStatus === "ACCEPTED" || item.followStatus === "PENDING";

              return (
                <li key={item.userId} className="flex items-center justify-between gap-2">
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
                    onClick={() => handleFollowToggle(item.userId, item.followStatus)}
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
            })}
          </ul>
        )}
        {!expanded && suggestedItems.length > 0 ? (
          <button
            type="button"
            onClick={expand}
            className="mt-4 block w-full text-center text-sm font-medium text-primary hover:underline"
          >
            Xem tất cả gợi ý
          </button>
        ) : null}
      </div>
    </aside>
  );
}