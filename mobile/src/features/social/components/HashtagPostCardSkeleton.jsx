import { View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

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
    media: {
      width: "34%",
      backgroundColor: colors.surfaceContainerHigh,
      minHeight: 148,
    },
    body: {
      flex: 1,
      padding: 14,
      gap: 10,
    },
    authorRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
    },
    avatar: {
      width: 32,
      height: 32,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerHigh,
    },
    authorLine: {
      flex: 1,
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    line: {
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    lineShort: {
      width: "66%",
    },
    footer: {
      marginTop: "auto",
      flexDirection: "row",
      gap: 16,
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 10,
    },
    stat: {
      width: 48,
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
  };
}

export function HashtagPostCardSkeleton() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.card}>
      <View style={styles.media} />
      <View style={styles.body}>
        <View style={styles.authorRow}>
          <View style={styles.avatar} />
          <View style={styles.authorLine} />
        </View>
        <View style={styles.line} />
        <View style={[styles.line, styles.lineShort]} />
        <View style={styles.footer}>
          <View style={styles.stat} />
          <View style={styles.stat} />
        </View>
      </View>
    </View>
  );
}
