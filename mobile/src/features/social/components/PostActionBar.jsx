import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { formatSocialCount } from "../utils/formatSocialCount";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { LikeCountButton } from "./LikeCountButton";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      backgroundColor: colors.surface,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    left: {
      flexDirection: "row",
      alignItems: "center",
      gap: 16,
    },
    likeGroup: {
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
    },
    actionButton: {
      flexDirection: "row",
      alignItems: "center",
      gap: 6,
      minHeight: 44,
      minWidth: 44,
      justifyContent: "center",
    },
    countText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurfaceVariant,
    },
  };
}

export function PostActionBar({
  postId,
  likedByMe = false,
  likeCount = 0,
  replyCount = 0,
  allowComments = true,
  isLiking = false,
  onToggleLike,
  onOpenComments,
  onOpenLikesList,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.row}>
      <View style={styles.left}>
        <View style={styles.likeGroup}>
          <Pressable
            style={styles.actionButton}
            onPress={() => onToggleLike?.(postId)}
            disabled={isLiking || !onToggleLike}
            accessibilityRole="button"
            accessibilityLabel={likedByMe ? "Bo thich bai viet" : "Thich bai viet"}
            accessibilityState={{ selected: likedByMe }}
          >
            <Ionicons
              name={likedByMe ? "heart" : "heart-outline"}
              size={22}
              color={likedByMe ? colors.primary : colors.onSurfaceVariant}
            />
          </Pressable>
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
        </View>

        <Pressable
          style={styles.actionButton}
          onPress={() => onOpenComments?.(postId)}
          disabled={!onOpenComments}
          accessibilityRole="button"
          accessibilityLabel="Binh luan"
        >
          <Ionicons name="chatbubble-outline" size={22} color={colors.onSurfaceVariant} />
          <Text style={styles.countText}>{formatSocialCount(replyCount) ?? "0"}</Text>
        </Pressable>
      </View>
    </View>
  );
}
