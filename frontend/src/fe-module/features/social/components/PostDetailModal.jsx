import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useCurrentUserAvatarUrl } from "../../auth/hooks/useCurrentUserAvatarUrl";
import { useViewCommerceProduct } from "../hooks/useViewCommerceProduct";
import { useEnrichedProductTags } from "../hooks/useEnrichedProductTags";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { useCommentMediaUpload } from "../hooks/useCommentMediaUpload";
import { usePostComments } from "../hooks/usePostComments";
import { usePostDetail } from "../hooks/usePostDetail";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { isPostVideoMedia } from "../utils/postMediaType";
import { MediaGalleryLightbox } from "./MediaGalleryLightbox";
import { PostCaption } from "./PostCaption";
import { PostDetailComments } from "./PostDetailComments";
import { PostMediaCarousel } from "./PostMediaCarousel";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useVideoPlayback } from "../context/VideoPlaybackContext";
import { VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { LikeCountButton } from "./LikeCountButton";
import { CommentComposer } from "./CommentComposer";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";

import { PostActionRow } from "./PostActionRow";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";
const COMING_SOON = "Tính năng đang được phát triển.";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  return String(num);
}

function formatFullTimestamp(dateString) {
  if (!dateString) return "";
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return "";
    const timeStr = d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const dateStr = d.toLocaleDateString([], { month: "short", day: "numeric", year: "numeric" });
    return `${timeStr} · ${dateStr}`;
  } catch {
    return "";
  }
}

export function PostDetailModal({
  postId,
  focusComments,
  onClose,
  onToast,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  onToggleLikePost,
  onOpenLikesList,
  isSavingPost = false,
  isLikingPost = false,
  isDeletingPost = false,
  onViewProfile,
  onHashtagClick,
}) {
  const viewerAvatar = useCurrentUserAvatarUrl(DEFAULT_AVATAR);
  const viewCommerceProduct = useViewCommerceProduct();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const { pauseAll } = useVideoPlayback();
  const topLevelMediaUpload = useCommentMediaUpload();
  const { mediaItems: topLevelMediaItems, resetMedia: resetTopLevelMedia } = topLevelMediaUpload;
  const commentAnchorRef = useRef(null);
  const commentInputRef = useRef(null);
  const [galleryIndex, setGalleryIndex] = useState(null);
  const [commentGallery, setCommentGallery] = useState(null);
  const [draftComment, setDraftComment] = useState("");
  const [replyCountBump, setReplyCountBump] = useState(0);

  const { post, isLoading, isError, errorMessage, errorCode, retry } = usePostDetail(postId);
  const postAuthorDisplay = usePostAuthorDisplay(post?.authorId || post?.author?.userId, post?.author);
  const enrichedProductTags = useEnrichedProductTags(post?.productTags);
  const [savedByMe, setSavedByMe] = useState(false);
  const [likedByMe, setLikedByMe] = useState(false);
  const [likeCount, setLikeCount] = useState(0);

  useEffect(() => {
    setSavedByMe(post?.savedByMe ?? false);
  }, [post?.savedByMe, postId]);

  useEffect(() => {
    setLikedByMe(post?.likedByMe ?? false);
    setLikeCount(Number(post?.likeCount) || 0);
  }, [post?.likeCount, post?.likedByMe, postId]);

  const bumpReplyCount = useCallback((delta = 1) => {
    setReplyCountBump((value) => value + delta);
  }, []);

  const commentsEnabled = Boolean(post && !isError);
  const commentsState = usePostComments(postId, commentsEnabled, {
    onReplyCountChange: bumpReplyCount,
  });

  const showComingSoon = () => onToast?.(COMING_SOON);

  const handleToggleLike = useCallback(async () => {
    if (!postId || isLikingPost || !onToggleLikePost) return;

    const data = await onToggleLikePost(postId);
    if (data) {
      setLikedByMe(Boolean(data.liked));
      setLikeCount(Number(data.likeCount) || 0);
    }
  }, [isLikingPost, onToggleLikePost, postId]);

  useEffect(() => {
    setDraftComment("");
    setReplyCountBump(0);
    setCommentGallery(null);
    resetTopLevelMedia();
  }, [postId, resetTopLevelMedia]);

  useEffect(() => {
    if (!postId) return;
    pauseAll();
  }, [pauseAll, postId]);

  const handleClose = useCallback(() => {
    pauseAll();
    onClose?.();
  }, [onClose, pauseAll]);

  const displayReplyCount = (post?.replyCount ?? 0) + replyCountBump;
  const commentsDisabled = post?.allowComments === false || isWriteBlocked;

  const handleSubmitTopLevel = async () => {
    commentsState.clearSubmitError();
    const result = await commentsState.submitTopLevel(
      draftComment,
      topLevelMediaItems
    );
    if (result?.ok) {
      setDraftComment("");
      resetTopLevelMedia();
      onToast?.("Đã gửi bình luận.");
    } else if (commentsState.submitError) {
      onToast?.(commentsState.submitError);
    }
  };

  const handleDeleteComment = useCallback(
    async (commentId, parentCommentId) => {
      const result = await commentsState.deleteComment(commentId, { parentCommentId });
      if (result?.ok) {
        onToast?.(
          result.notFound ? "Bình luận không còn tồn tại." : "Đã xóa bình luận."
        );
        return;
      }
      if (result?.cancelled) return;
      if (result?.message) {
        onToast?.(result.message);
      }
    },
    [commentsState, onToast]
  );

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
        if (commentGallery) {
          pauseAll();
          setCommentGallery(null);
          return;
        }
        if (galleryIndex !== null) {
          pauseAll();
          setGalleryIndex(null);
          return;
        }
        handleClose();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [commentGallery, galleryIndex, handleClose, pauseAll]);

  useEffect(() => {
    if (!focusComments || !post || commentsState.isLoading) return;
    const timer = window.setTimeout(() => {
      commentInputRef.current?.focus({ preventScroll: true });
      commentAnchorRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 100);
    return () => window.clearTimeout(timer);
  }, [focusComments, post, commentsState.isLoading]);

  const openGallery = (index = 0) => {
    if (!post?.media?.length) return;
    pauseAll();
    setCommentGallery(null);
    setGalleryIndex(index);
  };

  const openCommentGallery = useCallback(
    (commentId, media, index = 0) => {
      if (!media?.length) return;
      pauseAll();
      setGalleryIndex(null);
      setCommentGallery({ commentId, media, index });
    },
    [pauseAll],
  );

  return (
    <>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-2 md:p-6"
        role="presentation"
        onClick={handleClose}
      >
        {/* Floating circular close button on dark backdrop top-left */}
        <button
          type="button"
          onClick={handleClose}
          className="fixed top-4 left-4 z-[60] flex h-10 w-10 items-center justify-center rounded-full bg-black/60 text-white transition-colors hover:bg-black/80"
          aria-label="Đóng"
        >
          <span className="material-symbols-outlined text-xl" aria-hidden="true">
            close
          </span>
        </button>

        <div
          className="relative flex h-[92vh] w-[92vw] max-w-[1400px] flex-col overflow-hidden rounded-2xl border border-outline-variant/30 bg-surface-container-lowest shadow-2xl md:flex-row"
          role="dialog"
          aria-modal="true"
          aria-labelledby="post-detail-title"
          onClick={(event) => event.stopPropagation()}
        >
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
                onClick={handleClose}
                className="mt-3 text-sm font-medium text-primary hover:underline"
              >
                Đóng
              </button>
            </div>
          ) : null}

          {post && !isError ? (
            <>
              {/* Left column: media full-bleed black background */}
              {post.media?.length > 0 ? (
                <div className="relative flex w-full items-center justify-center bg-black overflow-hidden md:h-full md:w-3/5 lg:w-2/3 shrink-0">
                  <PostMediaCarousel
                    media={post.media}
                    postId={postId}
                    surface={VIDEO_PLAYBACK_SURFACES.DETAIL}
                    className="w-full h-full object-contain"
                    onMediaClick={(index) => {
                      const item = post.media[index];
                      if (item && !isPostVideoMedia(item)) {
                        openGallery(index);
                      }
                    }}
                  />
                </div>
              ) : (
                <div className="hidden bg-black md:flex md:w-3/5 lg:w-2/3 items-center justify-center text-zinc-600 shrink-0">
                  <span className="material-symbols-outlined text-5xl">image</span>
                </div>
              )}

              {/* Right column: scrollable content (7 items spec) */}
              <div className="flex h-full w-full flex-col bg-surface-container-lowest md:w-2/5 lg:w-1/3 min-w-0 border-l border-outline-variant/30">
                {/* 1. Header: Author avatar + Name + @handle + menu */}
                <div className="sticky top-0 z-10 flex items-center gap-3 border-b border-outline-variant/30 bg-surface-container-lowest/95 backdrop-blur-md px-4 py-3 shrink-0">
                  <button
                    type="button"
                    onClick={() => onViewProfile?.(post.author?.userId)}
                    className="shrink-0"
                    aria-label="Xem hồ sơ tác giả"
                  >
                    <img
                      src={postAuthorDisplay.avatarUrl}
                      alt=""
                      className="h-10 w-10 rounded-full object-cover"
                    />
                  </button>
                  <button
                    type="button"
                    onClick={() => onViewProfile?.(post.author?.userId)}
                    className="min-w-0 flex-1 text-left"
                  >
                    <h2
                      id="post-detail-title"
                      className="truncate text-[15px] font-bold text-on-surface hover:underline leading-tight"
                    >
                      {postAuthorDisplay.displayName}
                    </h2>
                    <p className="truncate text-xs text-on-surface-variant/70">
                      {postAuthorDisplay.handle}
                    </p>
                  </button>
                  <PostOptionsMenu
                    postId={post.postId}
                    isOwner={Boolean(post.isOwner)}
                    savedByMe={savedByMe}
                    icon="more_horiz"
                    className="rounded-full text-on-surface hover:bg-surface-container-high"
                    onEdit={() => onEdit?.(post.postId)}
                    onDelete={() => onDeletePost?.(post.postId)}
                    onToggleSave={async () => {
                      const data = await onToggleSavePost?.(post.postId);
                      if (data?.saved !== undefined) {
                        setSavedByMe(data.saved);
                      }
                    }}
                    isSaving={isSavingPost}
                    isDeleting={isDeletingPost}
                  />
                </div>

                <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
                  {/* 2. Text content: full text caption, not truncated */}
                  {post.caption || (post.hashtags && post.hashtags.length > 0) ? (
                    <div>
                      <PostCaption
                        caption={post.caption}
                        hashtags={post.hashtags}
                        onHashtagClick={onHashtagClick}
                      />
                    </div>
                  ) : null}

                  {/* 3. Attached products */}
                  {enrichedProductTags.length > 0 ? (
                    <PostProductTagsBlock
                      tags={enrichedProductTags}
                      variant="detail"
                      onViewProduct={viewCommerceProduct}
                    />
                  ) : null}

                  {/* 4. Timestamp + Views */}
                  <div className="flex items-center gap-1 text-xs text-on-surface-variant/70 py-2 border-t border-outline-variant/20">
                    <span>{formatFullTimestamp(post.createdAt) || formatRelativeTime(post.createdAt)}</span>
                    <span>·</span>
                    <span className="font-bold text-on-surface">{formatCount(post.viewCount || 0)}</span>
                    <span>Lượt xem</span>
                  </div>

                  {/* 5. Action Row (semantic hover colors) */}
                  <PostActionRow
                    post={{
                      ...post,
                      likedByMe,
                      savedByMe,
                      likeCount,
                      replyCount: displayReplyCount,
                    }}
                    onReplyClick={() => commentInputRef.current?.focus()}
                    onComingSoon={showComingSoon}
                    onToggleLikePost={handleToggleLike}
                    onOpenLikesList={onOpenLikesList}
                    onToggleSavePost={async (pId) => {
                      const data = await onToggleSavePost?.(pId);
                      if (data?.saved !== undefined) {
                        setSavedByMe(data.saved);
                      }
                    }}
                    isLikingPost={isLikingPost}
                    isSavingPost={isSavingPost}
                    variant="detail"
                  />

                  {/* 6. Comment list (sub post rows) */}
                  <div ref={commentAnchorRef} className="pt-2">
                    <PostDetailComments
                      commentsState={commentsState}
                      onViewProfile={onViewProfile}
                      onOpenLikesList={onOpenLikesList}
                      commentInputRef={commentInputRef}
                      onDeleteComment={handleDeleteComment}
                      onOpenCommentMedia={openCommentGallery}
                    />
                  </div>
                </div>

                {/* 7. Bottom pinned comment input */}
                <div className="sticky bottom-0 border-t border-outline-variant/30 bg-surface-container-lowest p-3 shrink-0">
                  <div className="flex items-start gap-3">
                    <img
                      src={viewerAvatar}
                      alt=""
                      className="mt-1 h-8 w-8 shrink-0 rounded-full object-cover"
                    />
                    <CommentComposer
                      inputRef={commentInputRef}
                      value={draftComment}
                      onChange={setDraftComment}
                      onSubmit={handleSubmitTopLevel}
                      onClearError={commentsState.clearSubmitError}
                      mediaUpload={topLevelMediaUpload}
                      placeholder={
                        isWriteBlocked
                          ? "Tài khoản bị đình chỉ"
                          : commentsDisabled
                            ? "Bình luận đã tắt"
                            : "Thêm bình luận..."
                      }
                      disabled={commentsDisabled}
                      isSubmitting={commentsState.isSubmittingTopLevel}
                    />
                  </div>
                  {commentsState.submitError && !commentsState.replyingToId ? (
                    <p className="mt-2 px-1 text-xs text-error" role="alert">
                      {commentsState.submitError}
                    </p>
                  ) : null}
                </div>
              </div>
            </>
          ) : null}
        </div>
      </div>

      {galleryIndex !== null && post?.media ? (
        <MediaGalleryLightbox
          media={post.media}
          postId={postId}
          initialIndex={galleryIndex}
          onClose={() => setGalleryIndex(null)}
        />
      ) : null}

      {commentGallery ? (
        <MediaGalleryLightbox
          media={commentGallery.media}
          ownerId={commentGallery.commentId}
          initialIndex={commentGallery.index}
          onClose={() => setCommentGallery(null)}
        />
      ) : null}
    </>
  );
}
