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
import { PostMediaItem } from "./PostMediaItem";
import { MediaGalleryLightbox } from "./MediaGalleryLightbox";
import { PostCaption } from "./PostCaption";
import { PostDetailComments } from "./PostDetailComments";
import { PostMediaGrid } from "./PostMediaGrid";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useVideoPlayback } from "../context/VideoPlaybackContext";
import { buildPlaybackId, VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { LikeCountButton } from "./LikeCountButton";
import { CommentComposer } from "./CommentComposer";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";
const COMING_SOON = "Tính năng đang được phát triển.";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  return String(num);
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
  const [draftComment, setDraftComment] = useState("");
  const [replyCountBump, setReplyCountBump] = useState(0);

  const { post, isLoading, isError, errorMessage, errorCode, retry } = usePostDetail(postId);
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
  }, [galleryIndex, handleClose, pauseAll]);

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
    setGalleryIndex(index);
  };

  return (
    <>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-on-background/40 p-4 backdrop-blur-md md:p-8"
        role="presentation"
        onClick={handleClose}
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
            onClick={handleClose}
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
                onClick={handleClose}
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
                  className={[
                    "relative min-h-[280px] w-full overflow-hidden bg-surface-container-high md:min-h-full md:w-1/2",
                    isPostVideoMedia(post.media[0]) ? "" : "cursor-pointer",
                  ]
                    .filter(Boolean)
                    .join(" ")}
                  onClick={
                    isPostVideoMedia(post.media[0]) ? undefined : () => openGallery(0)
                  }
                  onKeyDown={
                    isPostVideoMedia(post.media[0])
                      ? undefined
                      : (event) => {
                          if (event.key === "Enter") openGallery(0);
                        }
                  }
                  role={isPostVideoMedia(post.media[0]) ? undefined : "button"}
                  tabIndex={isPostVideoMedia(post.media[0]) ? undefined : 0}
                  aria-label={
                    isPostVideoMedia(post.media[0]) ? "Video bài viết" : "Mở gallery ảnh"
                  }
                >
                  <PostMediaItem
                    item={post.media[0]}
                    variant="inline"
                    className="h-full min-h-[280px] w-full object-cover md:min-h-full"
                    playbackId={buildPlaybackId(
                      postId,
                      0,
                      VIDEO_PLAYBACK_SURFACES.DETAIL
                    )}
                  />
                </div>
              ) : (
                <div className="hidden min-h-[200px] bg-surface-container-high md:block md:w-1/2" />
              )}

              <div className="flex max-h-[70vh] w-full flex-col bg-surface-container-lowest md:w-1/2 md:max-h-[921px]">
                <div className="sticky top-0 z-10 flex items-center gap-3 border-b border-outline-variant bg-surface-container-lowest p-6">
                  <button
                    type="button"
                    onClick={() => onViewProfile?.(post.author?.userId)}
                    className="shrink-0"
                    aria-label="Xem hồ sơ tác giả"
                  >
                    <img
                      src={post.author?.avatarUrl || DEFAULT_AVATAR}
                      alt=""
                      className="h-12 w-12 rounded-full border border-outline-variant object-cover"
                    />
                  </button>
                  <button
                    type="button"
                    onClick={() => onViewProfile?.(post.author?.userId)}
                    className="min-w-0 flex-1 text-left"
                  >
                    <h2
                      id="post-detail-title"
                      className="truncate text-xl font-semibold text-on-surface hover:text-primary"
                    >
                      {post.author?.displayName || DEFAULT_USER_DISPLAY_NAME}
                    </h2>
                    <p className="text-sm text-on-surface-variant">
                      {formatRelativeTime(post.createdAt)}
                    </p>
                  </button>
                  <PostOptionsMenu
                    postId={post.postId}
                    isOwner={Boolean(post.isOwner)}
                    savedByMe={savedByMe}
                    icon="more_vert"
                    className="rounded-full text-primary hover:bg-primary-fixed"
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

                <div className="flex-1 overflow-y-auto p-6">
                  {post.caption || (post.hashtags && post.hashtags.length > 0) ? (
                    <div className="mb-6">
                      <PostCaption
                        caption={post.caption}
                        hashtags={post.hashtags}
                        onHashtagClick={onHashtagClick}
                      />
                    </div>
                  ) : null}

                  {post.media?.length > 1 ? (
                    <div className="mb-6 overflow-hidden rounded-lg">
                      <PostMediaGrid
                        media={post.media}
                        postId={postId}
                        surface={VIDEO_PLAYBACK_SURFACES.DETAIL}
                        onMediaClick={(index) => openGallery(index)}
                      />
                    </div>
                  ) : null}

                  {enrichedProductTags.length > 0 ? (
                    <PostProductTagsBlock
                      tags={enrichedProductTags}
                      variant="detail"
                      onViewProduct={viewCommerceProduct}
                    />
                  ) : null}

                  <div className="mb-6 flex items-center gap-6 border-y border-outline-variant py-3">
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        className="flex items-center text-on-surface-variant hover:text-primary disabled:opacity-50"
                        onClick={handleToggleLike}
                        disabled={isLikingPost || isWriteBlocked || !onToggleLikePost}
                        aria-label={likedByMe ? "Bỏ thích bài viết" : "Thích bài viết"}
                        aria-pressed={likedByMe}
                      >
                        <span
                          className={`material-symbols-outlined ${likedByMe ? "fill text-primary" : ""}`}
                          aria-hidden="true"
                        >
                          {likedByMe ? "favorite" : "favorite_border"}
                        </span>
                      </button>
                      <LikeCountButton
                        count={likeCount}
                        showZero
                        onPress={() =>
                          onOpenLikesList?.({
                            type: "post",
                            targetId: postId,
                            likeCount,
                          })
                        }
                      />
                    </div>
                    <button
                      type="button"
                      className="flex items-center gap-2 text-on-surface-variant hover:text-primary"
                      onClick={() => commentInputRef.current?.focus()}
                    >
                      <span className="material-symbols-outlined" aria-hidden="true">
                        chat_bubble_outline
                      </span>
                      <span className="text-sm font-medium">{formatCount(displayReplyCount)}</span>
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
                      onViewProfile={onViewProfile}
                      onOpenLikesList={onOpenLikesList}
                      commentInputRef={commentInputRef}
                      onDeleteComment={handleDeleteComment}
                    />
                  </div>
                </div>

                <div className="sticky bottom-0 border-t border-outline-variant bg-surface-container-lowest p-3">
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
    </>
  );
}
