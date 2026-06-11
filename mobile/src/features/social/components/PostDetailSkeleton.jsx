import { StyleSheet, View } from "react-native";
import { CommentSkeleton } from "./CommentSkeleton";
import { colors } from "../../../shared/theme/colors";

export function PostDetailSkeleton() {
  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.avatar} />
        <View style={styles.headerLines}>
          <View style={styles.lineMd} />
          <View style={styles.lineSm} />
        </View>
      </View>
      <View style={styles.media} />
      <View style={styles.statsRow}>
        <View style={styles.stat} />
        <View style={styles.stat} />
      </View>
      <View style={styles.caption} />
      <CommentSkeleton />
      <CommentSkeleton />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 16,
  },
  header: {
    flexDirection: "row",
    gap: 12,
    alignItems: "center",
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.surfaceContainerHigh,
  },
  headerLines: {
    flex: 1,
    gap: 8,
  },
  lineMd: {
    width: "50%",
    height: 16,
    borderRadius: 4,
    backgroundColor: colors.surfaceContainerHigh,
  },
  lineSm: {
    width: "30%",
    height: 12,
    borderRadius: 4,
    backgroundColor: colors.surfaceContainerHigh,
  },
  media: {
    width: "100%",
    aspectRatio: 1,
    borderRadius: 12,
    backgroundColor: colors.surfaceContainerHigh,
  },
  statsRow: {
    flexDirection: "row",
    gap: 24,
    paddingVertical: 8,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: colors.outlineVariant,
  },
  stat: {
    width: 64,
    height: 20,
    borderRadius: 4,
    backgroundColor: colors.surfaceContainerHigh,
  },
  caption: {
    width: "100%",
    height: 48,
    borderRadius: 8,
    backgroundColor: colors.surfaceContainerHigh,
  },
});
