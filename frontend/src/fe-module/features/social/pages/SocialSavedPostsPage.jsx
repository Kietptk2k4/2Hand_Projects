import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toggleSavePost } from "../api/savePostApi";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { FeedToast } from "../components/FeedToast";
import { EditPostModal } from "../components/EditPostModal";
import { PostDetailModal } from "../components/PostDetailModal";
import { SavedPostCard } from "../components/SavedPostCard";
import { SavedPostCardSkeleton } from "../components/SavedPostCardSkeleton";
import { SavedPostsHeader } from "../components/SavedPostsHeader";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { useSavedPosts } from "../hooks/useSavedPosts";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function SocialSavedPostsPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const [unsavingId, setUnsavingId] = useState(null);
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
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

  const viewProfile = useCallback(
    (profileUserId) => {
      if (!profileUserId) return;
      navigate(buildSocialProfilePath(profileUserId));
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

  const handleUnsave = useCallback(
    async (targetPostId) => {
      if (!targetPostId || unsavingId) return;

      removeItem(targetPostId);
      setUnsavingId(targetPostId);

      try {
        const result = await toggleSavePost(targetPostId);
        if (result?.saved) {
          refetch();
        }
      } catch (error) {
        refetch();
        setToastMessage(error?.message || "Không thể bỏ lưu bài viết.");
      } finally {
        setUnsavingId(null);
      }
    },
    [refetch, removeItem, unsavingId]
  );

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <FeedLeftSidebar />

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
                  isUnsaveLoading={unsavingId === post.postId}
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

        <FeedRightSidebar onComingSoon={showComingSoon} onViewProfile={viewProfile} />
      </div>

      {isOpen ? (
        <PostDetailModal
          key={`${postId}-${detailRefreshKey}`}
          postId={postId}
          focusComments={focusComments}
          onClose={closePost}
          onToast={setToastMessage}
          onEdit={openEdit}
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

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </>
  );
}
