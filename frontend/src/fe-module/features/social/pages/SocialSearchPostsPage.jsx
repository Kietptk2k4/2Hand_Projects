import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { FeedPostSkeleton } from "../components/FeedPostSkeleton";
import { FeedToast } from "../components/FeedToast";
import { EditPostModal } from "../components/EditPostModal";
import { PostCard } from "../components/PostCard";
import { LikesListModal } from "../components/LikesListModal";
import { PostDetailModal } from "../components/PostDetailModal";
import { useLikesListModal } from "../hooks/useLikesListModal";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { FeedLeftSidebar } from "../components/FeedLeftSidebar";
import { FeedRightSidebar } from "../components/FeedRightSidebar";
import { SearchResultsHeader } from "../components/SearchResultsHeader";
import { ExploreDefaultOverview } from "../components/ExploreDefaultOverview";
import { SearchSidebar } from "../components/SearchSidebar";
import { useEditPostModal } from "../hooks/useEditPostModal";
import { usePostActions } from "../hooks/usePostActions";
import { usePostDetailModal } from "../hooks/usePostDetailModal";
import { useSearchPosts } from "../hooks/useSearchPosts";
import { useViewCommerceProduct } from "../hooks/useViewCommerceProduct";
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
  const likesListModal = useLikesListModal();
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
    patchLiked,
  } = useSearchPosts();

  const resolvedUserId = currentUserId || user?.id;

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

  const displayKeyword = keyword || q;
  const emptyQuery = !q;
  const emptyResults = !emptyQuery && !isInitialLoading && !errorMessage && items.length === 0;

  return (
    <>
      <div className="mx-auto grid w-full max-w-[1360px] grid-cols-1 gap-4 px-2 py-0 min-h-screen md:px-4 lg:grid-cols-12 lg:gap-6">
        <FeedLeftSidebar onComingSoon={showComingSoon} />

        <section className="min-h-screen flex-1 border-x border-outline-variant/40 bg-surface-container-lowest lg:col-span-6">
          <SearchResultsHeader
            keyword={displayKeyword}
            totalElements={totalElements}
            onSearch={handleSelectKeyword}
            onClear={() => navigate(APP_ROUTES.socialSearchPosts)}
          />

          {emptyQuery ? (
            <ExploreDefaultOverview
              onSelectKeyword={handleSelectKeyword}
              onSelectHashtag={viewHashtag}
              onOpenPost={openPost}
            />
          ) : null}

          {!emptyQuery && isInitialLoading ? (
            <div className="divide-y divide-outline-variant/40">
              <FeedPostSkeleton />
              <FeedPostSkeleton />
            </div>
          ) : null}

          {!emptyQuery && !isInitialLoading && errorMessage ? (
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

          {emptyResults ? (
            <div className="p-8 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                search_off
              </span>
              <p className="text-sm text-on-surface-variant/70">
                Không tìm thấy bài viết cho &quot;<span className="text-sky-500 font-semibold">{displayKeyword}</span>&quot;
              </p>
            </div>
          ) : null}

          {!emptyQuery && !isInitialLoading && !errorMessage && items.length > 0 ? (
            <div className="flex flex-col">
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

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </>
  );
}
