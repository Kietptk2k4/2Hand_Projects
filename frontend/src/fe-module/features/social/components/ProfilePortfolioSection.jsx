import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { PostCard } from "./PostCard";
import { isPostVideoMedia } from "../utils/postMediaType";

function ProfileMediaGrid({ postsWithMedia, onOpenPost }) {
  if (!postsWithMedia || postsWithMedia.length === 0) {
    return (
      <div className="p-8 text-center">
        <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
          perm_media
        </span>
        <p className="text-sm text-on-surface-variant/70">Chưa có hình ảnh hoặc video nào được đăng.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-3 gap-1 md:gap-2 p-2">
      {postsWithMedia.map((post) => {
        const firstMedia = post.media?.[0];
        const mediaUrl = firstMedia?.url || firstMedia?.mediaUrl;
        const isVideo = isPostVideoMedia(firstMedia);
        const isMulti = (post.media?.length || 0) > 1;

        return (
          <button
            key={post.postId}
            type="button"
            onClick={() => onOpenPost(post.postId)}
            className="group relative aspect-square overflow-hidden bg-surface-container-high rounded-xl cursor-pointer transition-transform hover:scale-[1.02] shadow-2xs"
            aria-label="Xem bài viết chi tiết"
          >
            {isVideo ? (
              <>
                <video src={mediaUrl} className="h-full w-full object-cover" muted playsInline />
                <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-black/30">
                  <span className="material-symbols-outlined text-3xl text-white drop-shadow-md">
                    play_circle
                  </span>
                </div>
              </>
            ) : (
              <img
                src={mediaUrl}
                alt=""
                className="h-full w-full object-cover transition-opacity group-hover:opacity-90"
                loading="lazy"
              />
            )}

            {/* Multi-media Badge (nhiều ảnh/video trong 1 bài) */}
            {isMulti ? (
              <div className="pointer-events-none absolute right-2 top-2 flex items-center justify-center rounded-md bg-black/60 p-1 text-white backdrop-blur-xs shadow-sm">
                <span className="material-symbols-outlined text-[16px]">collections</span>
              </div>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}

export function ProfilePortfolioSection({
  postsState,
  onOpenPost,
  isPrivateLocked,
  isOwner = false,
  activeTab = "posts",
  profileName = "",
  profileUserId = null,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  onToggleLikePost,
  onOpenLikesList,
  isSavingPost,
  isLikingPost,
  isDeletingPost,
  onViewProfile,
  onHashtagClick,
  onViewProduct,
  currentUserId,
  onComingSoon,
}) {
  const {
    items,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = postsState;

  if (isPrivateLocked) {
    return (
      <div className="p-8 text-center">
        <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
          lock
        </span>
        <p className="text-base font-bold text-on-surface">
          Tài khoản riêng tư — theo dõi để xem bài viết
        </p>
        <p className="mt-1 text-sm text-on-surface-variant/70">
          Gửi yêu cầu theo dõi để xem bài viết của người dùng này.
        </p>
      </div>
    );
  }

  if (isInitialLoading) {
    return (
      <div className="divide-y divide-outline-variant/40">
        <FeedPostSkeleton />
        <FeedPostSkeleton />
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="m-4 rounded-2xl border border-error/30 bg-error-container/40 p-6 text-center">
        <p className="text-sm text-on-error-container">{errorMessage}</p>
        <button
          type="button"
          onClick={retry}
          className="mt-4 rounded-full bg-on-surface px-5 py-2 text-sm font-bold text-surface-container-lowest hover:opacity-90"
        >
          Thử lại
        </button>
      </div>
    );
  }

  // Tab 3: Media Grid (1 ô cho 1 bài viết chứa media -> click mở detail modal)
  if (activeTab === "media") {
    const postsWithMedia = items.filter((post) => post.media && post.media.length > 0);
    return <ProfileMediaGrid postsWithMedia={postsWithMedia} onOpenPost={onOpenPost} />;
  }

  const emptyMessages = {
    posts: "Chưa có bài viết nào.",
    replies: "Chưa có phản hồi nào.",
    media: "Chưa có hình ảnh hoặc video nào được đăng.",
    saved: "Bạn chưa lưu bài viết nào.",
  };

  const emptyIcons = {
    posts: "feed",
    replies: "forum",
    media: "perm_media",
    saved: "bookmark",
  };

  if (items.length === 0) {
    return (
      <div className="p-8 text-center">
        <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
          {emptyIcons[activeTab] || "feed"}
        </span>
        <p className="text-sm text-on-surface-variant/70">
          {emptyMessages[activeTab] || "Chưa có dữ liệu."}
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col">
      {items.map((item) => {
        const resolvedAuthorId =
          item.authorId ||
          item.author_id ||
          item.author?.userId ||
          item.author?.id ||
          profileUserId;
        const post = {
          ...item,
          authorId: resolvedAuthorId,
        };
        return (
          <div key={post.postId} className="relative">
            {activeTab === "replies" ? (
              <div className="flex items-center gap-1.5 px-4 pt-2.5 text-xs font-semibold text-on-surface-variant/70">
                <span className="material-symbols-outlined text-[16px] text-sky-500" aria-hidden="true">
                  chat_bubble
                </span>
                <span>{profileName ? `${profileName} đã bình luận` : "Đã bình luận"}</span>
              </div>
            ) : null}
            <PostCard
              post={post}
              currentUserId={currentUserId}
              onOpenPost={onOpenPost}
              onComingSoon={onComingSoon}
              onEdit={onEdit}
              onDeletePost={onDeletePost}
              onToggleSavePost={onToggleSavePost}
              onToggleLikePost={onToggleLikePost}
              onOpenLikesList={onOpenLikesList}
              isSavingPost={isSavingPost?.(post.postId)}
              isLikingPost={isLikingPost?.(post.postId)}
              isDeletingPost={isDeletingPost?.(post.postId)}
              onViewProfile={onViewProfile}
              onHashtagClick={onHashtagClick}
              onViewProduct={onViewProduct}
            />
          </div>
        );
      })}

      {hasNext ? (
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
    </div>
  );
}
