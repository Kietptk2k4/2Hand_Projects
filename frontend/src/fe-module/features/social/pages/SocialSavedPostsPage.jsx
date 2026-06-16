import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useFeedSidebarStats } from "../hooks/useFeedSidebarStats";
import { useToggleSaveWithSidebar } from "../hooks/useToggleSaveWithSidebar";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { FeedToast } from "../components/FeedToast";
import { EditPostModal } from "../components/EditPostModal";
import { LikesListModal } from "../components/LikesListModal";
import { PostDetailModal } from "../components/PostDetailModal";
import { useLikesListModal } from "../hooks/useLikesListModal";
import { SavedPostCard } from "../components/SavedPostCard";
import { SavedPostCardSkeleton } from "../components/SavedPostCardSkeleton";
import { SavedPostsHeader } from "../components/SavedPostsHeader";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { useSavedPosts } from "../hooks/useSavedPosts";
import { useViewCommerceProduct } from "../hooks/useViewCommerceProduct";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function SocialSavedPostsPage() {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const sidebarStats = useFeedSidebarStats(user?.id);
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const [unsavingId, setUnsavingId] = useState(null);
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const likesListModal = useLikesListModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const {
    items,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    loadMore,
    retry,
    refetch,
    removeItem,
  } = useSavedPosts();

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const viewProduct = useViewCommerceProduct();

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

  const onEditSuccess = useCallback(() => {
    refetch();
    if (postId) {
      setDetailRefreshKey((key) => key + 1);
    }
    setToastMessage("Cập nhật bài viết thành công.");
  }, [postId, refetch]);

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
      handleDeletePost(targetPostId, { onRemoved: removeItem });
    },
    [handleDeletePost, removeItem]
  );

  const onToggleSavePost = useToggleSaveWithSidebar({
    items,
    adjustSavedCount: sidebarStats.adjustSavedCount,
    handleToggleSavePost,
    getWasSaved: () => true,
    onSavedChange: (_id, saved) => {
      if (!saved) {
        removeItem(_id);
      }
    },
  });

  const onToggleLikePost = useCallback(
    (targetPostId) => handleToggleLikePost(targetPostId),
    [handleToggleLikePost]
  );

  const handleUnsave = useCallback(
    async (targetPostId) => {
      if (!targetPostId || unsavingId) return;

      removeItem(targetPostId);
      sidebarStats.adjustSavedCount(-1);
      setUnsavingId(targetPostId);

      try {
        const result = await handleToggleSavePost(targetPostId);
        if (result?.saved) {
          sidebarStats.adjustSavedCount(1);
          refetch();
        }
      } catch {
        sidebarStats.adjustSavedCount(1);
        refetch();
      } finally {
        setUnsavingId(null);
      }
    },
    [handleToggleSavePost, refetch, removeItem, sidebarStats, unsavingId]
  );

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <FeedLeftSidebar stats={sidebarStats} onComingSoon={showComingSoon} />

        <section className="flex flex-col gap-6 lg:col-span-6">
          <SavedPostsHeader />

          {isInitialLoading ? (
            <div className="flex flex-col gap-6">
              <SavedPostCardSkeleton />
              <SavedPostCardSkeleton />
              <SavedPostCardSkeleton />
            </div>
          ) : null}

          {!isInitialLoading && errorMessage ? (
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

          {!isInitialLoading && !errorMessage && items.length === 0 ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                bookmark
              </span>
              <p className="text-sm text-on-surface-variant">Bạn chưa lưu bài viết nào.</p>
            </div>
          ) : null}

          {!isInitialLoading && !errorMessage && items.length > 0 ? (
            <div className="flex flex-col gap-6">
              {items.map((post) => (
                <SavedPostCard
                  key={post.postId}
                  post={post}
                  onOpenPost={(id) => openPost(id)}
                  onOpenComments={(id) => openPost(id, { focusComments: true })}
                  onViewProfile={viewProfile}
                  onUnsave={handleUnsave}
                  onOpenLikesList={likesListModal.openLikesList}
                  isUnsaveLoading={unsavingId === post.postId}
                  onViewProduct={viewProduct}
                />
              ))}
            </div>
          ) : null}

          {!isInitialLoading && !errorMessage && hasNext ? (
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

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </>
  );
}
