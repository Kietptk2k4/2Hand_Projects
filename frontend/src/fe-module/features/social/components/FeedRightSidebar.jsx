import { useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useSuggestedUsers } from "../hooks/useSuggestedUsers";
import { useTrendingHashtags, formatTrendingPostCount } from "../hooks/useTrendingHashtags";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { SuggestedUserListItem } from "./SuggestedUserListItem";

export function FeedRightSidebar({ onComingSoon, onViewProfile, onSelectHashtag, onToast }) {
  const navigate = useNavigate();
  const {
    items: trendingItems,
    isLoading: isTrendingLoading,
    isError: isTrendingError,
    errorMessage: trendingErrorMessage,
  } = useTrendingHashtags({ limit: 5 });
  const {
    items: suggestedItems,
    isLoading: isSuggestionsLoading,
    isError: isSuggestionsError,
    errorMessage: suggestionsErrorMessage,
    hasMore: hasMoreSuggestions,
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

  const goToAllSuggestions = () => {
    navigate(APP_ROUTES.socialSuggestedUsers);
  };

  return (
    <aside className="hidden flex-col gap-6 lg:col-span-3 lg:flex lg:sticky lg:top-20 lg:max-h-[calc(100vh-5rem)] lg:self-start lg:overflow-y-auto">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Đang thịnh hành</h3>
        {isTrendingLoading ? (
          <p className="text-sm text-on-surface-variant">Đang tải...</p>
        ) : isTrendingError ? (
          <p className="text-sm text-error">{trendingErrorMessage}</p>
        ) : trendingItems.length === 0 ? (
          <p className="text-sm text-on-surface-variant">Chưa có hashtag thịnh hành.</p>
        ) : (
          <ul className="flex flex-col gap-3">
            {trendingItems.map((item) => (
              <li key={item.tag} className="flex flex-col">
                <button
                  type="button"
                  className="text-left text-sm font-medium text-primary hover:underline"
                  onClick={() => goToHashtag(item.tag)}
                >
                  #{item.tag}
                </button>
                <span className="text-xs font-semibold text-on-surface-variant">
                  {formatTrendingPostCount(item.postCount)}
                </span>
              </li>
            ))}
          </ul>
        )}
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
            {suggestedItems.map((item) => (
              <SuggestedUserListItem
                key={item.userId}
                item={item}
                onViewProfile={onViewProfile}
                onFollowToggle={handleFollowToggle}
                followButtonLabel={followButtonLabel}
                suggestionSubtitle={suggestionSubtitle}
                loadingUserId={loadingUserId}
                followDisabled={followDisabled}
              />
            ))}
          </ul>
        )}
        {hasMoreSuggestions && suggestedItems.length > 0 ? (
          <button
            type="button"
            onClick={goToAllSuggestions}
            className="mt-4 block w-full text-center text-sm font-medium text-primary hover:underline"
          >
            Xem tất cả gợi ý
          </button>
        ) : null}
      </div>
    </aside>
  );
}
