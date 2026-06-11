import { StyleSheet, View } from "react-native";
import { colors } from "../../../shared/theme/colors";

export function FeedPostSkeleton() {
  return (
    <View style={styles.card} accessibilityElementsHidden importantForAccessibility="no-hide-descendants">
      <View style={styles.header}>
        <View style={styles.avatar} />
        <View style={styles.headerText}>
          <View style={[styles.line, styles.lineShort]} />
          <View style={[styles.line, styles.lineTiny]} />
        </View>
      </View>
      <View style={[styles.line, styles.lineFull]} />
      <View style={[styles.line, styles.lineMedium]} />
      <View style={styles.media} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    padding: 16,
    marginBottom: 16,
  },
  header: {
    flexDirection: "row",
    gap: 12,
    marginBottom: 16,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.surfaceContainerHigh,
  },
  headerText: {
    flex: 1,
    gap: 8,
    justifyContent: "center",
  },
  line: {
    height: 12,
    borderRadius: 6,
    backgroundColor: colors.surfaceContainerHigh,
  },
  lineShort: {
    width: 128,
  },
  lineTiny: {
    width: 96,
  },
  lineFull: {
    width: "100%",
    marginBottom: 8,
  },
  lineMedium: {
    width: "66%",
    marginBottom: 16,
  },
  media: {
    height: 192,
    borderRadius: 12,
    backgroundColor: colors.surfaceContainerHigh,
  },
});
