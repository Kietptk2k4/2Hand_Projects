import { useCallback, useState } from "react";
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
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <FeedLeftSidebar onComingSoon={showComingSoon} />

        <section className="flex flex-col gap-6 lg:col-span-6">
          <SuggestedUsersHeader />

          {isInitialLoading ? (
            <div className="flex flex-col gap-4">
              <SuggestedUserRowSkeleton />
              <SuggestedUserRowSkeleton />
              <SuggestedUserRowSkeleton />
            </div>
          ) : null}

          {!isInitialLoading && isError ? (
            <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
              <p className="text-sm text-on-error-container">{errorMessage}</p>
              <button
                type="button"
                onClick={retry}
                className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
              >
                Thử lại
              </button>
            </div>
          ) : null}

          {!isInitialLoading && !isError && items.length === 0 ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                group
              </span>
              <p className="text-sm text-on-surface-variant">Chưa có gợi ý người dùng.</p>
            </div>
          ) : null}

          {!isInitialLoading && !isError && items.length > 0 ? (
            <ul className="flex flex-col gap-4">
              {items.map((item) => (
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
                  className="rounded-lg border border-primary px-6 py-2 text-sm font-medium text-primary transition-colors hover:bg-[#e7eeff]"
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
