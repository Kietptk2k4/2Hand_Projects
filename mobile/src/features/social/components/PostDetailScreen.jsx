import { useCallback, useEffect, useRef, useState } from "react";

import {
  Image,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  View,
} from "react-native";

import { Ionicons } from "@expo/vector-icons";

import { router } from "expo-router";

import { useSafeAreaInsets } from "react-native-safe-area-context";

import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";

import { useCurrentUserId } from "../hooks/useCurrentUserId";

import { useDeletePost } from "../hooks/useDeletePost";

import { useLikePost } from "../hooks/useLikePost";

import { usePostComments } from "../hooks/usePostComments";

import { usePostDetail } from "../hooks/usePostDetail";

import { useSavePost } from "../hooks/useSavePost";

import { formatRelativeTime } from "../utils/formatRelativeTime";

import { authorAvatarUrl } from "../utils/authorDisplay";

import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";

import { resolvePostIsOwner } from "../utils/resolvePostAuthorId";

import { CommentComposer } from "./CommentComposer";

import { PostActionBar } from "./PostActionBar";

import { PostCaption } from "./PostCaption";

import { PostDetailComments } from "./PostDetailComments";

import { PostDetailSkeleton } from "./PostDetailSkeleton";

import { PostMediaCarousel } from "./PostMediaCarousel";
import { MediaGalleryLightbox } from "./MediaGalleryLightbox";

import { PostOptionsMenu } from "./PostOptionsMenu";

import { ROUTES } from "../../../shared/constants/routes";

import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { resolvePostProductTags } from "../utils/mapProductTagsFromApi";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";

function createPostDetailStyles(colors) {
  return {
    flex: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    scrollContent: {
      padding: 16,
      paddingBottom: 24,
    },
    centered: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      padding: 24,
      backgroundColor: colors.surface,
    },
    errorMessage: {
      marginTop: 12,
      fontSize: 14,
      color: colors.onSurface,
      textAlign: "center",
    },
    primaryButton: {
      marginTop: 16,
      backgroundColor: colors.primary,
      borderRadius: 8,
      minHeight: 44,
      paddingHorizontal: 20,
      alignItems: "center",
      justifyContent: "center",
    },
    primaryButtonText: {
      color: colors.onPrimary,
      fontSize: 14,
      fontWeight: "600",
    },
    authorHeader: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      marginBottom: 16,
    },
    authorAvatar: {
      width: 48,
      height: 48,
      borderRadius: 24,
      backgroundColor: colors.surfaceContainerHigh,
    },
    authorInfo: {
      flex: 1,
      minWidth: 0,
    },
    authorName: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
    authorTime: {
      marginTop: 2,
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    captionBlock: {
      marginTop: 16,
    },
    composerBar: {
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 16,
      paddingTop: 8,
    },
    composerError: {
      marginTop: 4,
      fontSize: 12,
      color: colors.error,
    },
  };
}

export function PostDetailScreen({ postId, focusComments = false }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createPostDetailStyles);
  const insets = useSafeAreaInsets();

  const currentUserId = useCurrentUserId();

  const scrollRef = useRef(null);

  const commentAnchorRef = useRef(null);

  const commentInputRef = useRef(null);

  const [draftComment, setDraftComment] = useState("");

  const [replyCountBump, setReplyCountBump] = useState(0);

  const [galleryIndex, setGalleryIndex] = useState(null);



  const { post, isLoading, isError, errorMessage, errorCode, retry } =

    usePostDetail(postId);



  const { toggleLike, isLikingPost } = useLikePost();

  const { toggleSave, isSavingPost } = useSavePost();

  const { confirmDelete, isDeletingPost } = useDeletePost();



  const bumpReplyCount = useCallback((delta = 1) => {

    setReplyCountBump((value) => value + delta);

  }, []);



  const commentsEnabled = Boolean(post && !isError);

  const commentsState = usePostComments(postId, commentsEnabled, {

    onReplyCountChange: bumpReplyCount,

  });



  const displayReplyCount = (post?.replyCount ?? 0) + replyCountBump;

  const commentsDisabled = post?.allowComments === false;

  const isOwner = resolvePostIsOwner(post, currentUserId);
  const productTags = resolvePostProductTags(post);



  useEffect(() => {

    setDraftComment("");

    setReplyCountBump(0);

    setGalleryIndex(null);

  }, [postId]);



  useEffect(() => {

    if (!focusComments || !post || commentsState.isLoading) return;



    const timer = setTimeout(() => {

      scrollRef.current?.scrollToEnd({ animated: true });

      commentInputRef.current?.focus();

    }, 300);



    return () => clearTimeout(timer);

  }, [focusComments, post, commentsState.isLoading]);



  const onViewProfile = useCallback((userId) => {

    if (!userId) return;

    router.push(ROUTES.userProfile(userId));

  }, []);



  const onHashtagClick = useCallback((tag) => {

    if (!tag) return;

    router.push(ROUTES.hashtag(tag));

  }, []);

  const onOpenLikesList = useCallback(({ type, targetId, likeCount }) => {
    if (!targetId) return;
    router.push(
      ROUTES.postLikes(targetId, {
        targetType: type || "post",
        likeCount: likeCount ?? 0,
      })
    );
  }, []);



  const handleSubmitTopLevel = async () => {

    commentsState.clearSubmitError();

    const result = await commentsState.submitTopLevel(draftComment);

    if (result?.ok) {

      setDraftComment("");

    }

  };



  if (isLoading) {

    return <PostDetailSkeleton />;

  }



  if (isError) {

    return (

      <View style={styles.centered}>

        <Ionicons

          name={errorCode === 403 ? "lock-closed-outline" : "alert-circle-outline"}

          size={40}

          color={colors.error}

        />

        <Text style={styles.errorMessage}>{errorMessage}</Text>

        {errorCode !== 403 && errorCode !== 404 ? (

          <Pressable style={styles.primaryButton} onPress={retry}>

            <Text style={styles.primaryButtonText}>Thử lại</Text>

          </Pressable>

        ) : null}

      </View>

    );

  }



  if (!post) return null;



  const authorAvatar = resolveDevMediaUrl(
    post.author?.avatarUrl || authorAvatarUrl(post.author?.userId) || DEFAULT_AVATAR
  );

  const authorName = post.author?.displayName || DEFAULT_USER_DISPLAY_NAME;



  return (

    <KeyboardAvoidingView

      style={styles.flex}

      behavior={Platform.OS === "ios" ? "padding" : undefined}

      keyboardVerticalOffset={insets.top + 56}

    >

      <ScrollView

        ref={scrollRef}

        style={styles.flex}

        contentContainerStyle={styles.scrollContent}

        keyboardShouldPersistTaps="handled"

      >

        <View style={styles.authorHeader}>

          <Pressable onPress={() => onViewProfile(post.author?.userId)}>

            <Image source={{ uri: authorAvatar }} style={styles.authorAvatar} />

          </Pressable>

          <Pressable

            style={styles.authorInfo}

            onPress={() => onViewProfile(post.author?.userId)}

          >

            <Text style={styles.authorName} numberOfLines={1}>

              {authorName}

            </Text>

            <Text style={styles.authorTime}>{formatRelativeTime(post.createdAt)}</Text>

          </Pressable>

          {currentUserId ? (

            <PostOptionsMenu

              postId={post.postId}

              isOwner={isOwner}

              savedByMe={post.savedByMe}

              onEdit={() => router.push(ROUTES.postEdit(post.postId))}

              onDelete={() =>

                confirmDelete(post.postId, { navigateBack: true })

              }

              onToggleSave={() => toggleSave(post.postId)}

              isSaving={isSavingPost(post.postId)}

              isDeleting={isDeletingPost(post.postId)}

            />

          ) : null}

        </View>



        {post.media?.length > 0 ? (

          <PostMediaCarousel
            media={post.media}
            postId={post.postId}
            surface={VIDEO_PLAYBACK_SURFACES.DETAIL}
            onMediaPress={(index) => setGalleryIndex(index)}
          />

        ) : null}



        {post.caption || (post.hashtags && post.hashtags.length > 0) ? (

          <View style={styles.captionBlock}>

            <PostCaption

              caption={post.caption}

              hashtags={post.hashtags}

              onHashtagPress={onHashtagClick}

            />

          </View>

        ) : null}

        {productTags.length > 0 ? (
          <View style={styles.captionBlock}>
            <PostProductTagsBlock tags={productTags} variant="detail" />
          </View>
        ) : null}



        <PostActionBar

          postId={post.postId}

          likedByMe={post.likedByMe}

          likeCount={post.likeCount}

          replyCount={displayReplyCount}

          allowComments={post.allowComments}

          isLiking={isLikingPost(post.postId)}

          onToggleLike={toggleLike}

          onOpenComments={() => commentInputRef.current?.focus()}

          onOpenLikesList={onOpenLikesList}

        />



        <PostDetailComments

          commentsState={commentsState}

          onViewProfile={onViewProfile}

          commentAnchorRef={commentAnchorRef}

        />

      </ScrollView>



      <View style={[styles.composerBar, { paddingBottom: Math.max(insets.bottom, 8) }]}>

        <CommentComposer

          inputRef={commentInputRef}

          value={draftComment}

          onChange={setDraftComment}

          onSubmit={handleSubmitTopLevel}

          onClearError={commentsState.clearSubmitError}

          placeholder={

            commentsDisabled ? "Bình luận đã tắt" : "Thêm bình luận..."

          }

          disabled={commentsDisabled}

          isSubmitting={commentsState.isSubmittingTopLevel}

        />

        {commentsState.submitError && !commentsState.replyingToId ? (

          <Text style={styles.composerError} accessibilityRole="alert">

            {commentsState.submitError}

          </Text>

        ) : null}

      </View>



      {galleryIndex !== null && post.media?.length > 0 ? (
        <MediaGalleryLightbox
          media={post.media}
          postId={post.postId}
          initialIndex={galleryIndex}
          onClose={() => setGalleryIndex(null)}
        />
      ) : null}

    </KeyboardAvoidingView>

  );
}

