import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { colors } from "../../../shared/theme/colors";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function initialsFromName(name) {
  const parts = (name || "U").trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "U";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

export function FollowListRow({ item, onPress }) {
  const displayName = item.displayName || DEFAULT_USER_DISPLAY_NAME;

  return (
    <Pressable style={styles.row} onPress={() => onPress?.(item.userId)}>
      {item.avatarUrl ? (
        <Image source={{ uri: resolveDevMediaUrl(item.avatarUrl) }} style={styles.avatar} />
      ) : (
        <View style={styles.avatarFallback}>
          <Text style={styles.initials}>{initialsFromName(displayName)}</Text>
        </View>
      )}
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={1}>
          {displayName}
        </Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.outlineVariant,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
  },
  avatarFallback: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.surfaceContainerHigh,
    alignItems: "center",
    justifyContent: "center",
  },
  initials: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
  },
  info: {
    flex: 1,
    minWidth: 0,
  },
  name: {
    fontSize: 15,
    fontWeight: "600",
    color: colors.onSurface,
  },
});
