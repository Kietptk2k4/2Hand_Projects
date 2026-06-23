import {
  ActivityIndicator,
  Image,
  Pressable,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { formatSocialCount } from "../utils/formatSocialCount";
import { resolvePostAuthorId, resolvePostIsOwner } from "../utils/resolvePostAuthorId";
import { resolvePostProductTags } from "../utils/mapProductTagsFromApi";
import { PostCaption } from "./PostCaption";
import { PostMediaItem } from "./PostMediaItem";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { LikeCountButton } from "./LikeCountButton";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const PLACEHOLDER_COLOR = "#DEE8FF";

function createStyles(colors) {
  return {
    card: {
      flexDirection: "row",
      overflow: "hidden",
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      marginBottom: 16,
      minHeight: 148,
    },
    mediaPressable: {
      width: "34%",
      backgroundColor: colors.surfaceContainerHigh,
    },
    media: {
      width: "100%",
      height: "100%",
      minHeight: 148,
    },
    mediaPlaceholder: {
      flex: 1,
      minHeight: 148,
      backgroundColor: PLACEHOLDER_COLOR,
      alignItems: "center",
      justifyContent: "center",
    },
    body: {
      flex: 1,
      padding: 14,
    },
    topRow: {
      flexDirection: "row",
      alignItems: "flex-start",
      justifyContent: "space-between",
      gap: 8,
      marginBottom: 8,
    },
    authorRow: {
      flex: 1,
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
      minWidth: 0,
    },
    avatar: {
      width: 32,
      height: 32,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerHigh,
    },
    authorMeta: {
      flex: 1,
      minWidth: 0,
    },
    authorName: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurface,
    },
    authorTime: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      marginTop: 1,
    },
    captionWrap: {
      marginBottom: 8,
      flexGrow: 1,
    },
    footer: {
      marginTop: "auto",
      flexDirection: "row",
      alignItems: "center",
      gap: 16,
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 10,
    },
    statItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
    },
    likeBtn: {
      padding: 2,
    },
    statText: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
      fontWeight: "500",
    },
  };
}

export function HashtagPostCard({
  post,
  currentUserId,
  onOpenPost,
  onOpenComments,
  onViewProfile,
  onHashtagClick,
  onEditPost,
  onDeletePost,
  onToggleLike,
  onToggleSave,
  onOpenLikesList,
  isLikingPost = false,
  isSavingPost = false,
  isDeletingPost = false,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const authorId = resolvePostAuthorId(post);
  const author = usePostAuthorDisplay(authorId);
  const isOwner = resolvePostIsOwner(post, currentUserId);
  const savedByMe = post.savedByMe ?? false;
  const likedByMe = post.likedByMe ?? false;
  const primaryMedia = post.media?.[0];
  const productTags = resolvePostProductTags(post);

  return (
    <View style={styles.card}>
      <Pressable
        style={styles.mediaPressable}
        onPress={() => onOpenPost?.(post.postId)}
        accessibilityRole="button"
        accessibilityLabel="Xem chi tiết bài viết"
      >
        {primaryMedia ? (
          <PostMediaItem item={primaryMedia} variant="grid" style={styles.media} playIconSize={36} />
        ) : (
          <View style={styles.mediaPlaceholder}>
            <Ionicons name="image-outline" size={28} color={colors.outline} />
          </View>
        )}
      </Pressable>

      <View style={styles.body}>
        <View style={styles.topRow}>
          <Pressable style={styles.authorRow} onPress={() => onViewProfile?.(authorId)}>
            <Image source={{ uri: author.avatarUrl }} style={styles.avatar} />
            <View style={styles.authorMeta}>
              <Text style={styles.authorName} numberOfLines={1}>
                {author.displayName}
              </Text>
              <Text style={styles.authorTime}>{formatRelativeTime(post.createdAt)}</Text>
            </View>
          </Pressable>
          {currentUserId ? (
            <PostOptionsMenu
              postId={post.postId}
              isOwner={isOwner}
              savedByMe={savedByMe}
              onEdit={() => onEditPost?.(post.postId)}
              onDelete={() => onDeletePost?.(post.postId)}
              onToggleSave={() => onToggleSave?.(post.postId)}
              isSaving={isSavingPost}
              isDeleting={isDeletingPost}
            />
          ) : null}
        </View>

        <View style={styles.captionWrap}>
          <PostCaption
            caption={post.caption}
            hashtags={post.hashtags}
            onCaptionPress={() => onOpenPost?.(post.postId)}
            onHashtagPress={onHashtagClick}
          />
          {productTags.length > 0 ? (
            <PostProductTagsBlock tags={productTags} variant="compact" />
          ) : null}
        </View>

        <View style={styles.footer}>
          <View style={styles.statItem}>
            <Pressable
              style={styles.likeBtn}
              onPress={() => onToggleLike?.(post.postId)}
              disabled={isLikingPost || !onToggleLike}
              accessibilityRole="button"
              accessibilityLabel={likedByMe ? "Bỏ thích bài viết" : "Thích bài viết"}
            >
              {isLikingPost ? (
                <ActivityIndicator size="small" color={colors.primary} />
              ) : (
                <Ionicons
                  name={likedByMe ? "thumbs-up" : "thumbs-up-outline"}
                  size={16}
                  color={likedByMe ? colors.primary : colors.onSurfaceVariant}
                />
              )}
            </Pressable>
            <LikeCountButton
              count={post.likeCount}
              size="compact"
              showZero
              onPress={() =>
                onOpenLikesList?.({
                  type: "post",
                  targetId: post.postId,
                  likeCount: post.likeCount,
                })
              }
            />
          </View>
          <Pressable
            style={styles.statItem}
            onPress={() => onOpenComments?.(post.postId)}
            accessibilityRole="button"
            accessibilityLabel="Xem bình luận"
          >
            <Ionicons name="chatbubble-outline" size={16} color={colors.onSurfaceVariant} />
            <Text style={styles.statText}>
              {formatSocialCount(post.replyCount) ?? "0"}
            </Text>
          </Pressable>
        </View>
      </View>
    </View>
  );
}
