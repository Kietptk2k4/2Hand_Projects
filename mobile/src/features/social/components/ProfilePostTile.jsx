import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    tile: {
      flex: 1,
      aspectRatio: 1,
      margin: 6,
      borderRadius: 12,
      overflow: "hidden",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
    },
    media: {
      width: "100%",
      height: "100%",
    },
    menuWrap: {
      position: "absolute",
      top: 8,
      right: 8,
      zIndex: 2,
    },
    draftBadge: {
      position: "absolute",
      top: 8,
      left: 8,
      borderRadius: 4,
      backgroundColor: "rgba(255,255,255,0.92)",
      paddingHorizontal: 8,
      paddingVertical: 2,
    },
    draftText: {
      fontSize: 11,
      fontWeight: "700",
      color: colors.onSurfaceVariant,
    },
    multiBadge: {
      position: "absolute",
      bottom: 8,
      right: 8,
      flexDirection: "row",
      alignItems: "center",
      gap: 2,
      borderRadius: 4,
      backgroundColor: "rgba(0,0,0,0.55)",
      paddingHorizontal: 6,
      paddingVertical: 2,
    },
    multiText: {
      fontSize: 11,
      color: "#FFFFFF",
      fontWeight: "600",
    },
    emptyBody: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      padding: 12,
      gap: 8,
    },
    emptyCaption: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    playOverlay: {
      ...StyleSheet.absoluteFillObject,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: "rgba(0,0,0,0.25)",
    },
  };
}

export function ProfilePostTile({
  post,
  onOpenPost,
  isOwner = false,
  savedByMe = false,
  onEdit,
  onDeletePost,
  onToggleSave,
  isSavingPost = false,
  isDeletingPost = false,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const primaryMedia = post.media?.[0];
  const mediaUrl = primaryMedia ? getPostMediaUrl(primaryMedia) : "";
  const isVideo = primaryMedia ? isPostVideoMedia(primaryMedia) : false;
  const isDraft = post.status === "DRAFT";

  const openPost = () => onOpenPost?.(post.postId);

  return (
    <Pressable
      style={styles.tile}
      onPress={openPost}
      accessibilityRole="button"
      accessibilityLabel={post.caption ? `Xem bài: ${post.caption.slice(0, 40)}` : "Xem bài viết"}
    >
      {mediaUrl ? (
        <>
          <Image source={{ uri: mediaUrl }} style={styles.media} resizeMode="cover" />
          {isVideo ? (
            <View style={styles.playOverlay} pointerEvents="none">
              <Ionicons name="play-circle" size={40} color="#FFFFFF" />
            </View>
          ) : null}
        </>
      ) : (
        <View style={styles.emptyBody}>
          <Ionicons name="document-text-outline" size={28} color={colors.outline} />
          {post.caption ? (
            <Text style={styles.emptyCaption} numberOfLines={3}>
              {post.caption}
            </Text>
          ) : null}
        </View>
      )}

      <View style={styles.menuWrap}>
        <PostOptionsMenu
          postId={post.postId}
          isOwner={isOwner}
          savedByMe={savedByMe}
          onEdit={() => onEdit?.(post.postId)}
          onDelete={() => onDeletePost?.(post.postId)}
          onToggleSave={() => onToggleSave?.(post.postId)}
          isSaving={isSavingPost}
          isDeleting={isDeletingPost}
        />
      </View>

      {isDraft ? (
        <View style={styles.draftBadge}>
          <Text style={styles.draftText}>NHÁP</Text>
        </View>
      ) : null}

      {post.media?.length > 1 ? (
        <View style={styles.multiBadge}>
          <Ionicons name="copy-outline" size={12} color="#FFFFFF" />
          <Text style={styles.multiText}>{post.media.length}</Text>
        </View>
      ) : null}
    </Pressable>
  );
}
