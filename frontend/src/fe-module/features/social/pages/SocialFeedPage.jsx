import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { FEED_TABS } from "../constants/feedTabs";
import { useCreatePostModal } from "../hooks/useCreatePostModal";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { useFeed } from "../hooks/useFeed";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { CreatePostModal } from "../components/CreatePostModal";
import { EditPostModal } from "../components/EditPostModal";
import { FeedComposer } from "../components/FeedComposer";
import { SocialWriteBlockedBanner } from "../components/SocialWriteBlockedBanner";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedPostSkeleton } from "../components/FeedPostSkeleton";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { FeedTabs } from "../components/FeedTabs";
import { FeedToast } from "../components/FeedToast";
import { PostCard } from "../components/PostCard";
import { LikesListModal } from "../components/LikesListModal";
import { PostDetailModal } from "../components/PostDetailModal";
import { useLikesListModal } from "../hooks/useLikesListModal";
import { useViewCommerceProduct } from "../hooks/useViewCommerceProduct";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function SocialFeedPage() {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const [activeTab, setActiveTab] = useState(FEED_TABS.GLOBAL);
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const likesListModal = useLikesListModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const {
    isOpen: isCreateOpen,
    openFilePickerOnMount,
    openCreatePost,
    closeCreatePost,
  } = useCreatePostModal();
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
    patchSaved,
    patchLiked,
  } = useFeed(activeTab);

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

  const onToggleSavePost = useCallback(
    (targetPostId) => handleToggleSavePost(targetPostId, { onSavedChange: patchSaved }),
    [handleToggleSavePost, patchSaved]
  );

  const onToggleLikePost = useCallback(
    (targetPostId) => handleToggleLikePost(targetPostId, { onLikedChange: patchLiked }),
    [handleToggleLikePost, patchLiked]
  );

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

  const viewProduct = useViewCommerceProduct();

  const viewHashtag = useCallback(
    (tag) => {
      navigate(buildSocialHashtagPath(tag));
    },
    [navigate]
  );

  const handleHashtagFromModal = useCallback(
    (tag) => {
      closePost();
      viewHashtag(tag);
    },
    [closePost, viewHashtag]
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

  const onCreateSuccess = useCallback(
    (_created, { publish }) => {
      if (publish) {
        if (activeTab !== FEED_TABS.GLOBAL) {
          setActiveTab(FEED_TABS.GLOBAL);
        }
        refetch();
        setToastMessage("Đăng bài thành công.");
      } else {
        setToastMessage("Đã lưu bản nháp.");
      }
    },
    [activeTab, refetch]
  );

  const emptyMessage =
    activeTab === FEED_TABS.FOLLOWING
      ? "Bạn chưa theo dõi ai hoặc chưa có bài viết từ người bạn theo dõi."
      : "Chưa có bài viết công khai nào trên feed đề xuất.";

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <FeedLeftSidebar onComingSoon={() => setToastMessage(COMING_SOON_MESSAGE)} />

        <section className="flex flex-col gap-6 lg:col-span-6">
          <FeedTabs activeTab={activeTab} onChange={setActiveTab} />
          <SocialWriteBlockedBanner />
          <FeedComposer
            onOpenCreatePost={() => openCreatePost()}
            onOpenCreatePostWithFilePicker={() => openCreatePost({ filePicker: true })}
          />

          {isInitialLoading ? (
            <div className="flex flex-col gap-6">
              <FeedPostSkeleton />
              <FeedPostSkeleton />
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
                feed
              </span>
              <p className="text-sm text-on-surface-variant">{emptyMessage}</p>
            </div>
          ) : null}

          {!isInitialLoading && !errorMessage && items.length > 0 ? (
            <div className="flex flex-col gap-6">
              {items.map((post) => (
                <PostCard
                  key={post.postId}
                  post={post}
                  currentUserId={user?.id}
                  onOpenPost={openPost}
                  onComingSoon={showComingSoon}
                  onEdit={openEdit}
                  onDeletePost={onDeletePost}
                  onToggleSavePost={onToggleSavePost}
                  onToggleLikePost={onToggleLikePost}
                  onOpenLikesList={likesListModal.openLikesList}
                  isSavingPost={isSavingPost(post.postId)}
                  isLikingPost={isLikingPost(post.postId)}
                  isDeletingPost={isDeletingPost(post.postId)}
                  onViewProfile={viewProfile}
                  onHashtagClick={viewHashtag}
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
          onHashtagClick={handleHashtagFromModal}
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
          onSuccess={onCreateSuccess}
          onToast={setToastMessage}
        />
      ) : null}

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </>
  );
}
