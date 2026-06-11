import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { resolvePostAuthorId, resolvePostIsOwner } from "../utils/resolvePostAuthorId";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { PostCaption } from "./PostCaption";
import { PostMediaCarousel } from "./PostMediaCarousel";
import { PostActionBar } from "./PostActionBar";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { colors } from "../../../shared/theme/colors";

export function PostCard({
  post,
  currentUserId,
  onOpenPost,
  onViewProfile,
  onHashtagClick,
  onToggleLike,
  onToggleSave,
  onEditPost,
  onDeletePost,
  isLikingPost = false,
  isSavingPost = false,
  isDeletingPost = false,
}) {
  const authorId = resolvePostAuthorId(post);
  const author = usePostAuthorDisplay(authorId);
  const isOwner = resolvePostIsOwner(post, currentUserId);

  const openDetail = (options) => {
    onOpenPost?.(post.postId, options);
  };

  return (
    <View style={styles.card}>
      <View style={styles.headerSection}>
        <View style={styles.header}>
          <Pressable
            onPress={() => onViewProfile?.(authorId)}
            style={styles.avatarButton}
            accessibilityLabel="Xem hồ sơ tác giả"
          >
            <Image source={{ uri: author.avatarUrl }} style={styles.avatar} />
          </Pressable>
          <Pressable
            style={styles.authorInfo}
            onPress={() => onViewProfile?.(authorId)}
          >
            <Text style={styles.authorName} numberOfLines={1}>
              {author.displayName}
            </Text>
            <Text style={styles.authorSubtitle}>Thành viên 2Hands</Text>
            <View style={styles.timeRow}>
              <Ionicons name="time-outline" size={14} color={colors.outline} />
              <Text style={styles.timeText}>{formatRelativeTime(post.createdAt)}</Text>
            </View>
          </Pressable>
          {currentUserId ? (
            <PostOptionsMenu
              postId={post.postId}
              isOwner={isOwner}
              savedByMe={post.savedByMe}
              onEdit={() => onEditPost?.(post.postId)}
              onDelete={() => onDeletePost?.(post.postId)}
              onToggleSave={() => onToggleSave?.(post.postId)}
              isSaving={isSavingPost}
              isDeleting={isDeletingPost}
            />
          ) : null}
        </View>
      </View>

      <PostMediaCarousel media={post.media} onMediaPress={() => openDetail()} />

      <PostActionBar
        postId={post.postId}
        likedByMe={post.likedByMe}
        likeCount={post.likeCount}
        replyCount={post.replyCount}
        allowComments={post.allowComments}
        isLiking={isLikingPost}
        onToggleLike={onToggleLike}
        onOpenComments={() => openDetail({ focusComments: true })}
      />

      {post.caption || (post.hashtags && post.hashtags.length > 0) ? (
        <View style={styles.captionSection}>
          <PostCaption
            caption={post.caption}
            hashtags={post.hashtags}
            onCaptionPress={() => openDetail()}
            onHashtagClick={onHashtagClick}
          />
        </View>
      ) : null}

      <Pressable
        style={styles.openButton}
        onPress={() => openDetail()}
        accessibilityRole="button"
        accessibilityLabel="Xem chi tiết bài viết"
      >
        <Text style={styles.openButtonText}>Xem chi tiết</Text>
        <Ionicons name="chevron-forward" size={16} color={colors.primary} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    marginBottom: 16,
    overflow: "hidden",
  },
  headerSection: {
    padding: 16,
    paddingBottom: 12,
  },
  header: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 12,
  },
  avatarButton: {
    width: 48,
    height: 48,
  },
  avatar: {
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
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
  },
  authorSubtitle: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    marginTop: 2,
  },
  timeRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    marginTop: 4,
  },
  timeText: {
    fontSize: 12,
    color: colors.outline,
  },
  captionSection: {
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 4,
  },
  openButton: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 4,
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    minHeight: 44,
  },
  openButtonText: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.primary,
  },
});
