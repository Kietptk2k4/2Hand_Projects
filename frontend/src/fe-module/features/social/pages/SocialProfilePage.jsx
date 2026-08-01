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
import { usePublicUserProfile } from "../hooks/usePublicUserProfile";
import { useSocialProfile } from "../hooks/useSocialProfile";
import { useUserPosts } from "../hooks/useUserPosts";
import { useUserRepliedPosts } from "../hooks/useUserRepliedPosts";
import { useSavedPosts } from "../hooks/useSavedPosts";
import {
  resolvePublicProfileDetails,
  resolveSelfProfileDetails,
} from "../utils/resolveProfileDetails";
import { EditPostModal } from "../components/EditPostModal";
import { FollowListModal } from "../components/FollowListModal";
import { LikesListModal } from "../components/LikesListModal";
import { useFollowActions } from "../hooks/useFollowActions";
import { useFollowListModal } from "../hooks/useFollowListModal";
import { useLikesListModal } from "../hooks/useLikesListModal";
import { usePublicShopByUser } from "../../commerce/hooks/usePublicShopByUser";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { CreatePostModal } from "../components/CreatePostModal";
import { useCreatePostModal } from "../hooks/useCreatePostModal";
import { useFeedSidebarStats } from "../hooks/useFeedSidebarStats";
import { useViewCommerceProduct } from "../hooks/useViewCommerceProduct";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

const PROFILE_TABS = [
  { id: "posts", label: "Bài viết" },
  { id: "replies", label: "Bài viết & Phản hồi" },
  { id: "media", label: "Media" },
  { id: "saved", label: "Đã lưu" },
];

export function SocialProfilePage() {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const [statusFilter, setStatusFilter] = useState(DEFAULT_PROFILE_STATUS_FILTER);
  const [activeTab, setActiveTab] = useState("posts");

  const sidebarStats = useFeedSidebarStats(user?.id);
  const {
    isOpen: isCreateOpen,
    openFilePickerOnMount,
    openCreatePost,
    closeCreatePost,
  } = useCreatePostModal();

  const { profile, isLoading, isError, errorMessage, errorCode, retry: refetchProfile } =
    useSocialProfile(userId);
  const { profile: accountProfile } = useAccountProfile();

  const isSelfBySession = Boolean(user?.id && userId && user.id === userId);
  const isSelf = profile?.followStatus === "SELF" || isSelfBySession;

  const {
    publicProfile,
    isLoading: isPublicProfileLoading,
    isError: isPublicProfileError,
    errorMessage: publicProfileErrorMessage,
    errorCode: publicProfileErrorCode,
    retry: retryPublicProfile,
  } = usePublicUserProfile(userId, { enabled: Boolean(userId) && !isSelfBySession });

  const { shop: commerceShop } = usePublicShopByUser(userId, {
    enabled: Boolean(userId),
  });

  const profileDetails = isSelf
    ? resolveSelfProfileDetails(accountProfile, user)
    : resolvePublicProfileDetails(publicProfile);
  const accountCoverUrl = accountProfile?.profile?.cover_url ?? accountProfile?.profile?.coverUrl ?? "";
  const coverImageUrl =
    profile?.coverUrl || profile?.cover_url || (isSelf ? accountCoverUrl : "") || "";
  const canViewPosts = Boolean(profile?.canViewFullProfile);
  const effectiveStatusFilter = isSelf ? statusFilter : "published";

  const postsState = useUserPosts(userId, {
    enabled: canViewPosts,
    statusFilter: effectiveStatusFilter,
  });

  const repliedPostsState = useUserRepliedPosts(userId, {
    enabled: canViewPosts && activeTab === "replies",
  });

  const savedPostsState = useSavedPosts();

  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const likesListModal = useLikesListModal();
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

  const {
    handleDeletePost,
    handleToggleSavePost,
    handleToggleLikePost,
    isSavingPost,
    isLikingPost,
    isDeletingPost,
  } = usePostActions({
      onToast: setToastMessage,
      openPostId: postId,
      closePost,
    });

  const onDeletePost = useCallback(
    (targetPostId) => {
      handleDeletePost(targetPostId, {
        onRemoved: (pId) => {
          postsState.removeItem(pId);
          repliedPostsState.removeItem(pId);
          savedPostsState.removeItem(pId);
        },
      });
    },
    [handleDeletePost, postsState, repliedPostsState, savedPostsState]
  );

  const onToggleSavePost = useCallback(
    (targetPostId) =>
      handleToggleSavePost(targetPostId, {
        onSavedChange: (pId, saved) => {
          postsState.patchSaved(pId, saved);
          repliedPostsState.patchSaved(pId, saved);
          if (!saved) {
            savedPostsState.removeItem(pId);
          } else {
            savedPostsState.refetch();
          }
        },
      }),
    [handleToggleSavePost, postsState, repliedPostsState, savedPostsState]
  );

  const onToggleLikePost = useCallback(
    (targetPostId) =>
      handleToggleLikePost(targetPostId, {
        onLikedChange: (pId, liked, likeCount) => {
          postsState.patchLiked(pId, liked, likeCount);
          repliedPostsState.patchLiked(pId, liked, likeCount);
        },
      }),
    [handleToggleLikePost, postsState, repliedPostsState]
  );

  const onEditSuccess = useCallback(() => {
    postsState.refetch();
    repliedPostsState.refetch();
    savedPostsState.refetch();
    if (postId) {
      setDetailRefreshKey((key) => key + 1);
    }
    setToastMessage("Cập nhật bài viết thành công.");
  }, [postId, postsState, repliedPostsState, savedPostsState]);

  const viewProfile = useCallback(
    (profileUserId) => {
      if (profileUserId) navigate(buildSocialProfilePath(profileUserId));
    },
    [navigate]
  );

  const viewProduct = useViewCommerceProduct();

  const viewHashtag = useCallback(
    (tag) => {
      navigate(buildSocialHashtagPath(tag));
    },
    [navigate]
  );

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const totalPostCount = postsState.items?.length || profile?.postCount || 0;

  // Resolve active dataset based on active tab
  let activePostsState = postsState;
  if (activeTab === "replies") {
    activePostsState = repliedPostsState;
  } else if (activeTab === "media") {
    activePostsState = postsState;
  } else if (activeTab === "saved") {
    activePostsState = savedPostsState;
  }

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1360px] grid-cols-1 gap-4 px-2 py-0 min-h-screen md:px-4 lg:grid-cols-12 lg:gap-6">
        {/* Left Sidebar */}
        <FeedLeftSidebar
          stats={sidebarStats}
          onComingSoon={showComingSoon}
          onOpenCreatePost={() => openCreatePost()}
        />

        {/* Middle Column (Profile Feed) */}
        <section className="min-h-screen flex-1 border-x border-outline-variant/40 bg-surface-container-lowest lg:col-span-6">
          {/* Sticky Top Navigation Bar */}
          <div className="sticky top-0 z-20 flex items-center gap-6 border-b border-outline-variant/40 bg-surface-container-lowest/90 px-4 py-2.5 backdrop-blur-md">
            <Link
              to={APP_ROUTES.socialFeed}
              className="flex h-9 w-9 items-center justify-center rounded-full transition-colors hover:bg-surface-container-low"
              aria-label="Về feed"
            >
              <span className="material-symbols-outlined text-[22px] text-on-surface" aria-hidden="true">
                arrow_back
              </span>
            </Link>
            <div className="min-w-0 flex-1">
              <h2 className="truncate text-lg font-extrabold leading-tight text-on-surface">
                {profile?.displayName || "Hồ sơ"}
              </h2>
              <p className="text-xs text-on-surface-variant/70">
                {totalPostCount} Bài viết
              </p>
            </div>
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
            <div className="m-4 rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-error" aria-hidden="true">
                person_off
              </span>
              <p className="text-sm text-on-error-container">{errorMessage}</p>
              {errorCode !== 404 ? (
                <button
                  type="button"
                  onClick={refetchProfile}
                  className="mt-4 rounded-full bg-on-surface px-5 py-2 text-sm font-bold text-surface-container-lowest hover:opacity-90"
                >
                  Thử lại
                </button>
              ) : (
                <Link
                  to={APP_ROUTES.socialFeed}
                  className="mt-4 inline-block text-sm font-bold text-sky-500 hover:underline"
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
                coverImageUrl={coverImageUrl}
                bio={profileDetails.bio}
                website={profileDetails.website}
                socialLinks={profileDetails.socialLinks}
                showPrivateNotice={profileDetails.showPrivateNotice}
                isDetailsLoading={!isSelf && isPublicProfileLoading}
                detailsError={
                  !isSelf && isPublicProfileError && publicProfileErrorCode !== 404
                    ? publicProfileErrorMessage
                    : ""
                }
                onDetailsRetry={!isSelf && isPublicProfileError ? retryPublicProfile : undefined}
                onFollowClick={handleFollowToggle}
                isFollowLoading={isFollowLoading}
                followDisabled={followDisabled}
                followDisabledTitle={followDisabledTitle}
                onFollowersClick={() => openFollowList("followers")}
                onFollowingClick={() => openFollowList("following")}
                commerceShop={commerceShop}
              />

              {/* Profile Tabs */}
              <div className="flex border-b border-outline-variant/40 bg-surface-container-lowest">
                {PROFILE_TABS.map((tab) => {
                  const isActive = activeTab === tab.id;
                  return (
                    <button
                      key={tab.id}
                      type="button"
                      onClick={() => setActiveTab(tab.id)}
                      className="relative flex flex-1 items-center justify-center py-3.5 text-sm font-semibold transition-colors hover:bg-surface-container-low/50"
                      role="tab"
                      aria-selected={isActive}
                    >
                      <span className={isActive ? "font-bold text-on-surface" : "text-on-surface-variant/70"}>
                        {tab.label}
                      </span>
                      {isActive ? (
                        <span className="absolute bottom-0 h-1 w-16 rounded-full bg-sky-500" />
                      ) : null}
                    </button>
                  );
                })}
              </div>

              {/* Filter for owner (only in posts tab) */}
              {isSelf && canViewPosts && activeTab === "posts" ? (
                <div className="flex justify-end px-4 py-2 border-b border-outline-variant/20 bg-surface-container-lowest">
                  <ProfilePostsFilter
                    value={statusFilter}
                    onChange={setStatusFilter}
                    disabled={postsState.isInitialLoading}
                  />
                </div>
              ) : null}

              {/* Posts Portfolio Section */}
              <ProfilePortfolioSection
                postsState={activePostsState}
                onOpenPost={openPost}
                isPrivateLocked={!canViewPosts}
                isOwner={isSelf}
                activeTab={activeTab}
                profileName={profile?.displayName}
                profileUserId={userId}
                onEdit={openEdit}
                onDeletePost={onDeletePost}
                onToggleSavePost={onToggleSavePost}
                onToggleLikePost={onToggleLikePost}
                onOpenLikesList={likesListModal.openLikesList}
                isSavingPost={isSavingPost}
                isLikingPost={isLikingPost}
                isDeletingPost={isDeletingPost}
                onViewProfile={viewProfile}
                onHashtagClick={viewHashtag}
                onViewProduct={viewProduct}
                currentUserId={user?.id}
                onComingSoon={showComingSoon}
              />
            </>
          ) : null}
        </section>

        {/* Right Sidebar */}
        <FeedRightSidebar
          onComingSoon={showComingSoon}
          onViewProfile={viewProfile}
          onSelectHashtag={viewHashtag}
          onToast={setToastMessage}
        />
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
          onToggleLikePost={onToggleLikePost}
          onOpenLikesList={likesListModal.openLikesList}
          isSavingPost={isSavingPost(postId)}
          isLikingPost={isLikingPost(postId)}
          isDeletingPost={isDeletingPost(postId)}
          onViewProfile={viewProfile}
        />
      ) : null}

      <LikesListModal
        isOpen={likesListModal.isOpen}
        targetType={likesListModal.targetType}
        targetId={likesListModal.targetId}
        likeCount={likesListModal.likeCount}
        onClose={likesListModal.closeLikesList}
        onViewProfile={viewProfile}
      />

      {isEditOpen ? (
        <EditPostModal
          postId={editPostId}
          onClose={closeEdit}
          onSuccess={onEditSuccess}
          onToast={setToastMessage}
        />
      ) : null}

      {isCreateOpen ? (
        <CreatePostModal
          openFilePickerOnMount={openFilePickerOnMount}
          onClose={closeCreatePost}
          onSuccess={() => {
            postsState.refetch();
            repliedPostsState.refetch();
            setToastMessage("Đăng bài thành công.");
          }}
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
