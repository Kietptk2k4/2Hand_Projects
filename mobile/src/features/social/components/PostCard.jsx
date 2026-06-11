import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { formatSocialCount } from "../utils/formatSocialCount";
import { PostCaption } from "./PostCaption";
import { PostMediaCarousel } from "./PostMediaCarousel";
import { colors } from "../../../shared/theme/colors";

export function PostCard({
  post,
  onOpenPost,
  onViewProfile,
  onHashtagClick,
}) {
  const author = usePostAuthorDisplay(post.authorId);

  const openDetail = (options) => {
    onOpenPost?.(post.postId, options);
  };

  return (
    <View style={styles.card}>
      <View style={styles.headerSection}>
        <View style={styles.header}>
          <Pressable
            onPress={() => onViewProfile?.(post.authorId)}
            style={styles.avatarButton}
            accessibilityLabel="Xem ho so tac gia"
          >
            <Image source={{ uri: author.avatarUrl }} style={styles.avatar} />
          </Pressable>
          <Pressable
            style={styles.authorInfo}
            onPress={() => onViewProfile?.(post.authorId)}
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
        </View>
      </View>

      <PostMediaCarousel media={post.media} onMediaPress={() => openDetail()} />

      <View style={styles.statsRow}>
        <View style={styles.statsLeft}>
          <View style={styles.statItem}>
            <Ionicons name="thumbs-up-outline" size={20} color={colors.onSurfaceVariant} />
            <Text style={styles.statText}>{formatSocialCount(post.likeCount) ?? "0"}</Text>
          </View>
          <Pressable
            style={styles.statItem}
            onPress={() => openDetail({ focusComments: true })}
            accessibilityRole="button"
            accessibilityLabel="Xem binh luan"
          >
            <Ionicons name="chatbubble-outline" size={20} color={colors.onSurfaceVariant} />
            <Text style={styles.statText}>{formatSocialCount(post.replyCount) ?? "0"}</Text>
          </Pressable>
        </View>
      </View>

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
        accessibilityLabel="Xem chi tiet bai viet"
      >
        <Text style={styles.openButtonText}>Xem chi tiet</Text>
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
  statsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    backgroundColor: colors.surface,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  statsLeft: {
    flexDirection: "row",
    gap: 16,
  },
  statItem: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    minHeight: 44,
  },
  statText: {
    fontSize: 14,
    fontWeight: "500",
    color: colors.onSurfaceVariant,
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
