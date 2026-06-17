import { Image, Pressable, Text, View } from "react-native";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { formatLikedAt } from "../utils/formatLikedAt";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function initialsFromName(name) {
  const parts = (name || "U").trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "U";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      paddingHorizontal: 8,
      paddingVertical: 12,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    profileBtn: {
      flex: 1,
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      minWidth: 0,
    },
    avatar: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
    },
    avatarFallback: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
      alignItems: "center",
      justifyContent: "center",
    },
    avatarFallbackText: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
    },
    info: {
      flex: 1,
      minWidth: 0,
    },
    name: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onSurface,
    },
    likedAt: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      marginTop: 2,
    },
  };
}

export function LikesListRow({ item, onViewProfile }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const displayName = item.displayName || DEFAULT_USER_DISPLAY_NAME;
  const likedLabel = formatLikedAt(item.likedAt);
  const avatarUrl = resolveDevMediaUrl(item.avatarUrl || DEFAULT_AVATAR);

  return (
    <View style={styles.row}>
      <Pressable
        style={styles.profileBtn}
        onPress={() => onViewProfile?.(item.userId)}
        accessibilityRole="button"
      >
        {item.avatarUrl ? (
          <Image source={{ uri: avatarUrl }} style={styles.avatar} />
        ) : (
          <View style={styles.avatarFallback}>
            <Text style={styles.avatarFallbackText}>{initialsFromName(displayName)}</Text>
          </View>
        )}
        <View style={styles.info}>
          <Text style={styles.name} numberOfLines={1}>
            {displayName}
          </Text>
          {likedLabel ? (
            <Text style={styles.likedAt}>Thích lúc {likedLabel}</Text>
          ) : null}
        </View>
      </Pressable>
    </View>
  );
}
