import { useCallback, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { FeedToast } from "../components/FeedToast";
import { SuggestedUserListItem } from "../components/SuggestedUserListItem";
import { SuggestedUsersHeader } from "../components/SuggestedUsersHeader";
import { useSuggestedUsersPage } from "../hooks/useSuggestedUsersPage";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

function SuggestedUserRowSkeleton() {
  return (
    <div className="flex animate-pulse items-center justify-between gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
      <div className="flex flex-1 items-center gap-3">
        <div className="h-10 w-10 rounded-full bg-surface-container-high" />
        <div className="flex flex-col gap-2">
          <div className="h-4 w-32 rounded bg-surface-container-high" />
          <div className="h-3 w-24 rounded bg-surface-container-high" />
        </div>
      </div>
      <div className="h-7 w-20 rounded-full bg-surface-container-high" />
    </div>
  );
}

export function SocialSuggestedUsersPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [userSearchQuery, setUserSearchQuery] = useState("");
  const {
    items,
    isInitialLoading,
    isLoadingMore,
    isError,
    errorMessage,
    hasNext,
    loadMore,
    retry,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled,
  } = useSuggestedUsersPage({ onToast: setToastMessage });

  const filteredItems = useMemo(() => {
    if (!userSearchQuery.trim()) return items;
    const query = userSearchQuery.trim().toLowerCase();
    return items.filter(
      (user) =>
        (user.name || "").toLowerCase().includes(query) ||
        (user.email || "").toLowerCase().includes(query) ||
        (user.username || "").toLowerCase().includes(query)
    );
  }, [items, userSearchQuery]);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const viewProfile = useCallback(
    (profileUserId) => {
      if (!profileUserId) return;
      navigate(buildSocialProfilePath(profileUserId));
    },
    [navigate]
  );

  const viewHashtag = useCallback(
    (tag) => {
      navigate(buildSocialHashtagPath(tag));
    },
    [navigate]
  );

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1360px] grid-cols-1 gap-4 px-2 py-0 min-h-screen md:px-4 lg:grid-cols-12 lg:gap-6">
        <FeedLeftSidebar onComingSoon={showComingSoon} />

        <section className="min-h-screen flex-1 border-x border-outline-variant/40 bg-surface-container-lowest lg:col-span-6">
          <SuggestedUsersHeader
            searchQuery={userSearchQuery}
            onSearchChange={setUserSearchQuery}
            onClearSearch={() => setUserSearchQuery("")}
          />

          {isInitialLoading ? (
            <div className="flex flex-col divide-y divide-outline-variant/40">
              <SuggestedUserRowSkeleton />
              <SuggestedUserRowSkeleton />
              <SuggestedUserRowSkeleton />
            </div>
          ) : null}

          {!isInitialLoading && isError ? (
            <div className="m-4 rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
              <p className="text-sm text-on-error-container">{errorMessage}</p>
              <button
                type="button"
                onClick={retry}
                className="mt-4 rounded-full bg-zinc-900 px-5 py-2 text-xs font-bold text-white hover:bg-zinc-800"
              >
                Thử lại
              </button>
            </div>
          ) : null}

          {!isInitialLoading && !isError && filteredItems.length === 0 ? (
            <div className="p-8 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                group
              </span>
              <p className="text-sm text-on-surface-variant/70">
                {userSearchQuery ? `Không tìm thấy thành viên phù hợp với "${userSearchQuery}".` : "Chưa có gợi ý người dùng."}
              </p>
            </div>
          ) : null}

          {!isInitialLoading && !isError && filteredItems.length > 0 ? (
            <ul className="flex flex-col">
              {filteredItems.map((item) => (
                <SuggestedUserListItem
                  key={item.userId}
                  item={item}
                  variant="page"
                  onViewProfile={viewProfile}
                  onFollowToggle={handleFollowToggle}
                  followButtonLabel={followButtonLabel}
                  suggestionSubtitle={suggestionSubtitle}
                  loadingUserId={loadingUserId}
                  followDisabled={followDisabled}
                />
              ))}
            </ul>
          ) : null}

          {!isInitialLoading && !isError && hasNext ? (
            <div className="flex justify-center py-4">
              {isLoadingMore ? (
                <div
                  className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                  aria-label="Đang tải thêm"
                />
              ) : (
                <button
                  type="button"
                  onClick={loadMore}
                  className="rounded-full border border-outline-variant px-6 py-2 text-sm font-semibold text-sky-500 transition-colors hover:bg-sky-500/10"
                >
                  Tải thêm
                </button>
              )}
            </div>
          ) : null}
        </section>

        <FeedRightSidebar
          onComingSoon={showComingSoon}
          onViewProfile={viewProfile}
          onSelectHashtag={viewHashtag}
          onToast={setToastMessage}
        />
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </>
  );
}
