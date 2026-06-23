import {
  ActivityIndicator,
  Image,
  Pressable,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { formatSavedAt } from "../utils/formatSavedAt";
import { formatSocialCount } from "../utils/formatSocialCount";
import { resolvePostAuthorId } from "../utils/resolvePostAuthorId";
import { resolvePostProductTags } from "../utils/mapProductTagsFromApi";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { PostMediaItem } from "./PostMediaItem";
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
    bookmarkBadge: {
      position: "absolute",
      top: 10,
      right: 10,
      flexDirection: "row",
      alignItems: "center",
      borderRadius: 999,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: "rgba(255,255,255,0.92)",
      paddingHorizontal: 8,
      paddingVertical: 4,
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
    authorName: {
      flex: 1,
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurface,
    },
    unsaveBtn: {
      width: 36,
      height: 36,
      alignItems: "center",
      justifyContent: "center",
    },
    title: {
      fontSize: 17,
      fontWeight: "600",
      color: colors.onSurface,
      marginBottom: 8,
    },
    footer: {
      marginTop: "auto",
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 10,
      gap: 8,
    },
    statsRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
    },
    statItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
    },
    statText: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
      fontWeight: "500",
    },
    savedLabel: {
      flexShrink: 1,
      fontSize: 11,
      fontWeight: "600",
      color: colors.secondary,
      textAlign: "right",
    },
  };
}

export function SavedPostCard({
  post,
  onOpenPost,
  onOpenComments,
  onViewProfile,
  onUnsave,
  onOpenLikesList,
  isUnsaveLoading = false,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const authorId = resolvePostAuthorId(post);
  const author = usePostAuthorDisplay(authorId);
  const primaryMedia = post.media?.[0];
  const savedLabel = formatSavedAt(post.savedAt || post.saved_at);
  const titleText = post.caption?.trim() || "Bài viết không có nội dung";
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
        <View style={styles.bookmarkBadge}>
          <Ionicons name="bookmark" size={14} color={colors.primary} />
        </View>
      </Pressable>

      <View style={styles.body}>
        <View style={styles.topRow}>
          <Pressable
            style={styles.authorRow}
            onPress={() => onViewProfile?.(authorId)}
          >
            <Image source={{ uri: author.avatarUrl }} style={styles.avatar} />
            <Text style={styles.authorName} numberOfLines={1}>
              {author.displayName}
            </Text>
          </Pressable>
          <Pressable
            style={styles.unsaveBtn}
            onPress={() => onUnsave?.(post.postId)}
            disabled={isUnsaveLoading}
            accessibilityRole="button"
            accessibilityLabel="Bỏ lưu bài viết"
          >
            {isUnsaveLoading ? (
              <ActivityIndicator size="small" color={colors.error} />
            ) : (
              <Ionicons name="bookmark-outline" size={22} color={colors.onSurfaceVariant} />
            )}
          </Pressable>
        </View>

        <Pressable onPress={() => onOpenPost?.(post.postId)}>
          <Text style={styles.title} numberOfLines={2}>
            {titleText}
          </Text>
        </Pressable>

        {productTags.length > 0 ? (
          <PostProductTagsBlock tags={productTags} variant="compact" />
        ) : null}

        <View style={styles.footer}>
          <View style={styles.statsRow}>
            <View style={styles.statItem}>
              <Ionicons name="thumbs-up-outline" size={16} color={colors.onSurfaceVariant} />
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
          {savedLabel ? (
            <Text style={styles.savedLabel} numberOfLines={2}>
              Đã lưu vào {savedLabel}
            </Text>
          ) : null}
        </View>
      </View>
    </View>
  );
}
