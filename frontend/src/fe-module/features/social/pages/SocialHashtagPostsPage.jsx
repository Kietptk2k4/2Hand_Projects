import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { FeedToast } from "../components/FeedToast";
import { EditPostModal } from "../components/EditPostModal";
import { HashtagPageHeader } from "../components/HashtagPageHeader";
import { HashtagPostCard } from "../components/HashtagPostCard";
import { HashtagPostCardSkeleton } from "../components/HashtagPostCardSkeleton";
import { HashtagSidebar } from "../components/HashtagSidebar";
import { PostDetailModal } from "../components/PostDetailModal";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { useHashtagPosts } from "../hooks/useHashtagPosts";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

export function SocialHashtagPostsPage() {
  const navigate = useNavigate();
  const { user } = useAuthSession();
  const currentUserId = useCurrentUserId();
  const [toastMessage, setToastMessage] = useState("");
  const [detailRefreshKey, setDetailRefreshKey] = useState(0);
  const { postId, focusComments, isOpen, openPost, closePost } = usePostDetailModal();
  const { editPostId, isEditOpen, openEdit, closeEdit } = useEditPostModal();
  const {
    hashtag,
    resolvedHashtag,
    isInvalidHashtag,
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
  } = useHashtagPosts();

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

  const displayHashtag = resolvedHashtag || hashtag;

  const navigateToHashtag = useCallback(
    (tag) => {
      navigate(buildSocialHashtagPath(tag));
    },
    [navigate]
  );

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

  const handleHashtagFromModal = useCallback(
    (tag) => {
      closePost();
      navigateToHashtag(tag);
    },
    [closePost, navigateToHashtag]
  );

  if (isInvalidHashtag) {
    return (
      <div className="mx-auto max-w-[1280px] px-4 py-16 text-center md:px-8">
        <p className="text-sm text-on-surface-variant">Hashtag không hợp lệ.</p>
        <Link to={APP_ROUTES.socialFeed} className="mt-4 inline-block text-sm font-medium text-primary hover:underline">
          Về feed
        </Link>
      </div>
    );
  }

  return (
    <>
      <HashtagPageHeader
        hashtag={displayHashtag}
        totalElements={totalElements}
        onSearchTag={navigateToHashtag}
      />

      <div className="mx-auto grid w-full max-w-[1280px] grid-cols-1 gap-6 px-4 py-8 md:px-8 lg:grid-cols-12">
        <HashtagSidebar currentHashtag={displayHashtag} onSelectTag={navigateToHashtag} />

        <section className="flex flex-col gap-6 lg:col-span-9">
          {isInitialLoading ? (
            <div className="flex flex-col gap-6">
              <HashtagPostCardSkeleton />
              <HashtagPostCardSkeleton />
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
                tag
              </span>
              <p className="text-sm text-on-surface-variant">
                Chưa có bài viết cho #{displayHashtag}
              </p>
            </div>
          ) : null}

          {!isInitialLoading && !errorMessage && items.length > 0 ? (
            <div className="flex flex-col gap-6">
              {items.map((post) => (
                <HashtagPostCard
                  key={post.postId}
                  post={post}
                  currentUserId={resolvedUserId}
                  onOpenPost={(id) => openPost(id)}
                  onOpenComments={(id) => openPost(id, { focusComments: true })}
                  onViewProfile={viewProfile}
                  onHashtagClick={navigateToHashtag}
                  onEdit={openEdit}
                  onDeletePost={onDeletePost}
                  onToggleSavePost={onToggleSavePost}
                  isSavingPost={isSavingPost(post.postId)}
                  isDeletingPost={isDeletingPost(post.postId)}
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
