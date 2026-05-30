import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { FeedPostSkeleton } from "../components/FeedPostSkeleton";
import { FeedToast } from "../components/FeedToast";
import { EditPostModal } from "../components/EditPostModal";
import { PostCard } from "../components/PostCard";
import { PostDetailModal } from "../components/PostDetailModal";
import { SearchResultsHeader } from "../components/SearchResultsHeader";
import { SearchSidebar } from "../components/SearchSidebar";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { useSearchPosts } from "../hooks/useSearchPosts";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { buildSocialSearchPath } from "../utils/socialSearchRoutes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function SocialSearchPostsPage() {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const currentUserId = useCurrentUserId();
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const {
    q,
    keyword,
    items,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    totalElements,
    loadMore,
    retry,
    refetch,
    removeItem,
    patchSaved,
  } = useSearchPosts();

  const resolvedUserId = currentUserId || user?.id;

  const { handleDeletePost, handleToggleSavePost, isSavingPost, isDeletingPost } =
    usePostActions({
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

  const handleSelectKeyword = useCallback(
    (nextKeyword) => {
      navigate(buildSocialSearchPath(nextKeyword));
    },
    [navigate]
  );

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

  const displayKeyword = keyword || q;
  const emptyQuery = !q;
  const emptyResults = !emptyQuery && !isInitialLoading && !errorMessage && items.length === 0;

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <SearchSidebar
          onSelectKeyword={handleSelectKeyword}
          onSelectHashtag={viewHashtag}
          refreshKey={displayKeyword}
        />

        <section className="flex flex-col gap-6 lg:col-span-9">
          <SearchResultsHeader keyword={displayKeyword} totalElements={totalElements} />

          {emptyQuery ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                search
              </span>
              <p className="text-sm text-on-surface-variant">Nhập từ khóa để tìm bài viết</p>
            </div>
          ) : null}

          {isInitialLoading ? (
            <div className="flex flex-col gap-6">
              <FeedPostSkeleton />
              <FeedPostSkeleton />
            </div>
          ) : null}

          {!emptyQuery && !isInitialLoading && errorMessage ? (
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

          {emptyResults ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                search_off
              </span>
              <p className="text-sm text-on-surface-variant">
                Không tìm thấy bài viết cho &quot;{displayKeyword}&quot;
              </p>
            </div>
          ) : null}

          {!emptyQuery && !isInitialLoading && !errorMessage && items.length > 0 ? (
            <div className="flex flex-col gap-6">
              {items.map((post) => (
                <PostCard
                  key={post.postId}
                  post={post}
                  currentUserId={resolvedUserId}
                  onOpenPost={openPost}
                  onComingSoon={showComingSoon}
                  onEdit={openEdit}
                  onDeletePost={onDeletePost}
                  onToggleSavePost={onToggleSavePost}
                  isSavingPost={isSavingPost(post.postId)}
                  isDeletingPost={isDeletingPost(post.postId)}
                  onViewProfile={viewProfile}
                  onHashtagClick={viewHashtag}
                />
              ))}
            </div>
          ) : null}

          {!emptyQuery && !isInitialLoading && !errorMessage && hasNext ? (
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
          onHashtagClick={handleHashtagFromModal}
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
