import { useCallback, useState } from "react";
import { FEED_TABS } from "../constants/feedTabs";
import { useCreatePostModal } from "../hooks/useCreatePostModal";
import { useFeed } from "../hooks/useFeed";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { CreatePostModal } from "../components/CreatePostModal";
import { FeedComposer } from "../components/FeedComposer";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedPostSkeleton } from "../components/FeedPostSkeleton";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { FeedTabs } from "../components/FeedTabs";
import { FeedToast } from "../components/FeedToast";
import { PostCard } from "../components/PostCard";
import { PostDetailModal } from "../components/PostDetailModal";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function SocialFeedPage() {
  const [activeTab, setActiveTab] = useState(FEED_TABS.GLOBAL);
  const [toastMessage, setToastMessage] = useState("");
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
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
  } = useFeed(activeTab);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

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
      : "Chưa có bài viết công khai nào trên feed toàn cầu.";

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <FeedLeftSidebar />

        <section className="flex flex-col gap-6 lg:col-span-6">
          <FeedTabs activeTab={activeTab} onChange={setActiveTab} />
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
                  onOpenPost={openPost}
                  onComingSoon={showComingSoon}
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

        <FeedRightSidebar onComingSoon={showComingSoon} />
      </div>

      {isOpen ? (
        <PostDetailModal
          postId={postId}
          focusComments={focusComments}
          onClose={closePost}
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
