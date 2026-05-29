import { useEffect, useRef, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { usePostComments } from "../hooks/usePostComments";
import { usePostDetail } from "../hooks/usePostDetail";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { formatVndPrice } from "../utils/formatPrice";
import { MediaGalleryLightbox } from "./MediaGalleryLightbox";
import { PostCaption } from "./PostCaption";
import { PostDetailComments } from "./PostDetailComments";
import { PostMediaGrid } from "./PostMediaGrid";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";
const COMING_SOON = "Tính năng đang được phát triển.";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  return String(num);
}

export function PostDetailModal({ postId, focusComments, onClose, onToast }) {
  const { user } = useAuthSession();
  const commentAnchorRef = useRef(null);
  const commentInputRef = useRef(null);
  const [galleryIndex, setGalleryIndex] = useState(null);

  const { post, isLoading, isError, errorMessage, errorCode, retry } = usePostDetail(postId);
  const commentsEnabled = Boolean(post && !isError);
  const commentsState = usePostComments(postId, commentsEnabled);

  const showComingSoon = () => onToast?.(COMING_SOON);

  useEffect(() => {
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, []);

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === "Escape") {
        if (galleryIndex !== null) {
          setGalleryIndex(null);
          return;
        }
        onClose?.();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [galleryIndex, onClose]);

  useEffect(() => {
    if (!focusComments || !post || commentsState.isLoading) return;
    const timer = window.setTimeout(() => {
      commentInputRef.current?.focus({ preventScroll: true });
      commentAnchorRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 100);
    return () => window.clearTimeout(timer);
  }, [focusComments, post, commentsState.isLoading]);

  const viewerAvatar =
    user?.avatar_url || user?.profile?.avatar_url || DEFAULT_AVATAR;

  const openGallery = (index = 0) => {
    if (!post?.media?.length) return;
    setGalleryIndex(index);
  };

  return (
    <>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-on-background/40 p-4 backdrop-blur-md md:p-8"
        role="presentation"
        onClick={onClose}
      >
        <div
          className="relative flex max-h-[92vh] w-full max-w-4xl flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg md:max-h-[921px] md:flex-row"
          role="dialog"
          aria-modal="true"
          aria-labelledby="post-detail-title"
          onClick={(event) => event.stopPropagation()}
        >
          <button
            type="button"
            onClick={onClose}
            className="absolute right-3 top-3 z-50 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface transition-colors hover:bg-surface-variant"
            aria-label="Đóng"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              close
            </span>
          </button>

          {isLoading ? (
            <div className="flex min-h-[320px] w-full items-center justify-center p-12 md:min-h-[480px]">
              <div
                className="h-10 w-10 animate-spin rounded-full border-4 border-surface-container-high border-t-primary"
                aria-label="Đang tải bài viết"
              />
            </div>
          ) : null}

          {isError ? (
            <div className="flex min-h-[280px] w-full flex-col items-center justify-center p-8 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-error" aria-hidden="true">
                {errorCode === 403 ? "lock" : "error_outline"}
              </span>
              <p className="text-sm text-on-surface">{errorMessage}</p>
              {errorCode !== 403 && errorCode !== 404 ? (
                <button
                  type="button"
                  onClick={retry}
                  className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
                >
                  Thử lại
                </button>
              ) : null}
              <button
                type="button"
                onClick={onClose}
                className="mt-3 text-sm font-medium text-primary hover:underline"
              >
                Đóng
              </button>
            </div>
          ) : null}

          {post && !isError ? (
            <>
              {post.media?.length > 0 ? (
                <div
                  className="relative min-h-[280px] w-full cursor-pointer bg-surface-container-high md:min-h-full md:w-1/2"
                  onClick={() => openGallery(0)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter") openGallery(0);
                  }}
                  role="button"
                  tabIndex={0}
                  aria-label="Mở gallery ảnh"
                >
                  <img
                    src={post.media[0].url}
                    alt=""
                    className="h-full min-h-[280px] w-full object-cover md:min-h-full"
                  />
                </div>
              ) : (
                <div className="hidden min-h-[200px] bg-surface-container-high md:block md:w-1/2" />
              )}

              <div className="flex max-h-[70vh] w-full flex-col bg-surface-container-lowest md:w-1/2 md:max-h-[921px]">
                <div className="sticky top-0 z-10 flex items-center gap-3 border-b border-outline-variant bg-surface-container-lowest p-6">
                  <img
                    src={post.author?.avatarUrl || DEFAULT_AVATAR}
                    alt=""
                    className="h-12 w-12 rounded-full border border-outline-variant object-cover"
                  />
                  <div className="min-w-0 flex-1">
                    <h2
                      id="post-detail-title"
                      className="truncate text-xl font-semibold text-on-surface"
                    >
                      {post.author?.displayName || "User"}
                    </h2>
                    <p className="text-sm text-on-surface-variant">
                      {formatRelativeTime(post.createdAt)}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="rounded-full p-2 text-primary hover:bg-primary-fixed"
                    onClick={showComingSoon}
                    aria-label="Tùy chọn"
                  >
                    <span className="material-symbols-outlined" aria-hidden="true">
                      more_vert
                    </span>
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto p-6">
                  {post.caption || (post.hashtags && post.hashtags.length > 0) ? (
                    <div className="mb-6">
                      <PostCaption caption={post.caption} hashtags={post.hashtags} />
                    </div>
                  ) : null}

                  {post.media?.length > 1 ? (
                    <div className="mb-6 overflow-hidden rounded-lg">
                      <PostMediaGrid
                        media={post.media}
                        onMediaClick={(index) => openGallery(index)}
                      />
                    </div>
                  ) : null}

                  {post.productTags?.length > 0 ? (
                    <div className="mb-6 flex items-center justify-between rounded-lg border border-outline-variant bg-surface-container-low p-3 shadow-sm">
                      <div className="flex items-center gap-3">
                        <div className="flex items-center justify-center rounded-lg bg-primary p-2 text-on-primary">
                          <span className="material-symbols-outlined" aria-hidden="true">
                            receipt_long
                          </span>
                        </div>
                        <div>
                          <h3 className="text-sm font-bold text-on-surface">Gói Tư vấn Thuế</h3>
                          <p className="text-sm text-on-surface-variant">
                            {formatVndPrice(post.productTags[0].price)}
                          </p>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={showComingSoon}
                        className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary shadow-sm hover:bg-[#0050cb]"
                      >
                        Book Now
                      </button>
                    </div>
                  ) : null}

                  <div className="mb-6 flex items-center gap-6 border-y border-outline-variant py-3">
                    <button
                      type="button"
                      className="flex items-center gap-2 text-on-surface-variant hover:text-primary"
                      onClick={showComingSoon}
                    >
                      <span
                        className={`material-symbols-outlined ${post.likedByMe ? "fill text-primary" : ""}`}
                        aria-hidden="true"
                      >
                        {post.likedByMe ? "favorite" : "favorite_border"}
                      </span>
                      <span className="text-sm font-medium">{formatCount(post.likeCount)}</span>
                    </button>
                    <button
                      type="button"
                      className="flex items-center gap-2 text-on-surface-variant hover:text-primary"
                      onClick={() => commentInputRef.current?.focus()}
                    >
                      <span className="material-symbols-outlined" aria-hidden="true">
                        chat_bubble_outline
                      </span>
                      <span className="text-sm font-medium">{formatCount(post.replyCount)}</span>
                    </button>
                    <button
                      type="button"
                      className="ml-auto text-on-surface-variant hover:text-primary"
                      onClick={showComingSoon}
                      aria-label="Chia sẻ"
                    >
                      <span className="material-symbols-outlined" aria-hidden="true">
                        share
                      </span>
                    </button>
                  </div>

                  <div ref={commentAnchorRef}>
                    <PostDetailComments
                      commentsState={commentsState}
                      onComingSoon={showComingSoon}
                      commentInputRef={commentInputRef}
                    />
                  </div>
                </div>

                <div className="sticky bottom-0 border-t border-outline-variant bg-surface-container-lowest p-3">
                  <div className="flex items-center gap-3">
                    <img src={viewerAvatar} alt="" className="h-8 w-8 rounded-full object-cover" />
                    <input
                      ref={commentInputRef}
                      type="text"
                      readOnly
                      onFocus={showComingSoon}
                      placeholder={
                        post.allowComments === false
                          ? "Bình luận đã tắt"
                          : "Thêm bình luận..."
                      }
                      disabled={post.allowComments === false}
                      className="flex-1 rounded-full border border-outline-variant bg-surface-container-low px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-60"
                    />
                    <button
                      type="button"
                      onClick={showComingSoon}
                      disabled={post.allowComments === false}
                      className="rounded-full p-2 text-primary hover:bg-primary-fixed disabled:opacity-50"
                      aria-label="Gửi bình luận"
                    >
                      <span className="material-symbols-outlined" aria-hidden="true">
                        send
                      </span>
                    </button>
                  </div>
                </div>
              </div>
            </>
          ) : null}
        </div>
      </div>

      {galleryIndex !== null && post?.media ? (
        <MediaGalleryLightbox
          media={post.media}
          initialIndex={galleryIndex}
          onClose={() => setGalleryIndex(null)}
        />
      ) : null}
    </>
  );
}
