import { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { FeedToast } from "../components/FeedToast";
import { PostDetailModal } from "../components/PostDetailModal";
import { ProfileHero } from "../components/ProfileHero";
import { ProfilePortfolioSection } from "../components/ProfilePortfolioSection";
import { ProfilePostsFilter } from "../components/ProfilePostsFilter";
import { DEFAULT_PROFILE_STATUS_FILTER } from "../constants/profilePostsConstants";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { useSocialProfile } from "../hooks/useSocialProfile";
import { useUserPosts } from "../hooks/useUserPosts";
import { EditPostModal } from "../components/EditPostModal";
import { FollowListModal } from "../components/FollowListModal";
import { useFollowActions } from "../hooks/useFollowActions";
import { useFollowListModal } from "../hooks/useFollowListModal";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

export function SocialProfilePage() {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const [statusFilter, setStatusFilter] = useState(DEFAULT_PROFILE_STATUS_FILTER);

  const { profile, isLoading, isError, errorMessage, errorCode, retry: refetchProfile } =
    useSocialProfile(userId);
  const { profile: accountProfile } = useAccountProfile();

  const isSelf = profile?.followStatus === "SELF";
  const canViewPosts = Boolean(profile?.canViewFullProfile);
  const effectiveStatusFilter = isSelf ? statusFilter : "published";

  const postsState = useUserPosts(userId, {
    enabled: canViewPosts,
    statusFilter: effectiveStatusFilter,
  });

  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const {
    isOpen: isFollowListOpen,
    activeType: followListType,
    openFollowList,
    closeFollowList,
    setActiveType: setFollowListType,
  } = useFollowListModal();

  const { handleFollowToggle, isFollowLoading, followDisabled, followDisabledTitle } =
    useFollowActions({
      userId,
      profile,
      onToast: setToastMessage,
      refetchProfile,
    });

  const bio =
    isSelf && accountProfile?.profile?.bio
      ? accountProfile.profile.bio
      : isSelf && user?.bio
        ? user.bio
        : "";

  const { handleDeletePost, handleToggleSavePost, isSavingPost, isDeletingPost } =
    usePostActions({
      onToast: setToastMessage,
      openPostId: postId,
      closePost,
    });

  const onDeletePost = useCallback(
    (targetPostId) => {
      handleDeletePost(targetPostId, { onRemoved: postsState.removeItem });
    },
    [handleDeletePost, postsState.removeItem]
  );

  const onToggleSavePost = useCallback(
    (targetPostId) =>
      handleToggleSavePost(targetPostId, { onSavedChange: postsState.patchSaved }),
    [handleToggleSavePost, postsState.patchSaved]
  );

  const onEditSuccess = useCallback(() => {
    postsState.refetch();
    if (postId) {
      setDetailRefreshKey((key) => key + 1);
    }
    setToastMessage("Cập nhật bài viết thành công.");
  }, [postId, postsState]);

  const viewProfile = useCallback(
    (profileUserId) => {
      if (profileUserId) navigate(buildSocialProfilePath(profileUserId));
    },
    [navigate]
  );

  return (
    <>
      <div className="mx-auto w-full max-w-[1280px] pb-12">
        <div className="mb-4 px-4 pt-4 md:px-8">
          <Link
            to={APP_ROUTES.socialFeed}
            className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              arrow_back
            </span>
            Về feed
          </Link>
        </div>

        {isLoading ? (
          <div className="flex min-h-[320px] items-center justify-center">
            <div
              className="h-10 w-10 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
              aria-label="Đang tải hồ sơ"
            />
          </div>
        ) : null}

        {isError ? (
          <div className="mx-4 rounded-xl border border-error/30 bg-error-container/40 p-8 text-center md:mx-8">
            <span className="material-symbols-outlined mb-2 text-4xl text-error" aria-hidden="true">
              person_off
            </span>
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            {errorCode !== 404 ? (
              <button
                type="button"
                onClick={refetchProfile}
                className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
              >
                Thử lại
              </button>
            ) : (
              <Link
                to={APP_ROUTES.socialFeed}
                className="mt-4 inline-block text-sm font-medium text-primary hover:underline"
              >
                Quay lại feed
              </Link>
            )}
          </div>
        ) : null}

        {profile && !isError ? (
          <>
            <ProfileHero
              profile={profile}
              bio={bio}
              onFollowClick={handleFollowToggle}
              isFollowLoading={isFollowLoading}
              followDisabled={followDisabled}
              followDisabledTitle={followDisabledTitle}
              onFollowersClick={() => openFollowList("followers")}
              onFollowingClick={() => openFollowList("following")}
            />

            <section className="mt-10 px-4 md:px-8">
              <div className="mb-4 flex flex-col gap-3 border-b border-outline-variant pb-4 sm:flex-row sm:items-center sm:justify-between">
                <h2 className="inline-block border-b-2 border-primary pb-2 text-lg font-semibold text-on-surface">
                  Bài viết
                </h2>
                {isSelf && canViewPosts ? (
                  <ProfilePostsFilter
                    value={statusFilter}
                    onChange={setStatusFilter}
                    disabled={postsState.isInitialLoading}
                  />
                ) : null}
              </div>

              <ProfilePortfolioSection
                postsState={postsState}
                onOpenPost={openPost}
                isPrivateLocked={!canViewPosts}
                isOwner={isSelf}
                onEdit={openEdit}
                onDeletePost={onDeletePost}
                onToggleSavePost={onToggleSavePost}
                isSavingPost={isSavingPost}
                isDeletingPost={isDeletingPost}
              />
            </section>
          </>
        ) : null}
      </div>

      {isOpen ? (
        <PostDetailModal
          key={`${postId}-${detailRefreshKey}`}
          postId={postId}
          focusComments={focusComments}
          onClose={closePost}
          onToast={setToastMessage}
          onEdit={openEdit}
          onDeletePost={onDeletePost}
          onToggleSavePost={onToggleSavePost}
          isSavingPost={isSavingPost(postId)}
          isDeletingPost={isDeletingPost(postId)}
          onViewProfile={viewProfile}
        />
      ) : null}

      {isEditOpen ? (
        <EditPostModal
          postId={editPostId}
          onClose={closeEdit}
          onSuccess={onEditSuccess}
          onToast={setToastMessage}
        />
      ) : null}

      {isFollowListOpen && profile ? (
        <FollowListModal
          isOpen={isFollowListOpen}
          targetUserId={userId}
          profile={profile}
          activeType={followListType}
          onClose={closeFollowList}
          onTypeChange={setFollowListType}
          onViewProfile={viewProfile}
        />
      ) : null}

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
