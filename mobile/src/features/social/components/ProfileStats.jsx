import { Pressable, StyleSheet, Text, View } from "react-native";
import { formatSocialCount } from "../utils/formatSocialCount";
import { colors } from "../../../shared/theme/colors";

export function ProfileStats({
  postCount,
  followerCount,
  followingCount,
  onFollowersPress,
  onFollowingPress,
}) {
  const followers = formatSocialCount(followerCount);
  const following = formatSocialCount(followingCount);
  const posts = formatSocialCount(postCount);

  return (
    <View style={styles.row}>
      {posts !== null ? (
        <View style={styles.stat}>
          <Text style={styles.value}>{posts}</Text>
          <Text style={styles.label}>Bài viết</Text>
        </View>
      ) : null}

      {followers !== null ? (
        <Pressable style={styles.stat} onPress={onFollowersPress}>
          <Text style={styles.value}>{followers}</Text>
          <Text style={styles.label}>Người theo dõi</Text>
        </Pressable>
      ) : null}

      {following !== null ? (
        <Pressable style={styles.stat} onPress={onFollowingPress}>
          <Text style={styles.value}>{following}</Text>
          <Text style={styles.label}>Đang theo dõi</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    justifyContent: "center",
    gap: 32,
    marginTop: 16,
  },
  stat: {
    alignItems: "center",
    minWidth: 72,
  },
  value: {
    fontSize: 18,
    fontWeight: "600",
    color: colors.onSurface,
  },
  label: {
    fontSize: 13,
    color: colors.onSurfaceVariant,
    marginTop: 2,
  },
});
