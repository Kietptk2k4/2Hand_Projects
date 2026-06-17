import { View } from "react-native";
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
      minHeight: 140,
    },
    thumb: {
      width: "34%",
      backgroundColor: colors.surfaceContainerHigh,
    },
    body: {
      flex: 1,
      padding: 14,
      gap: 10,
    },
    row: {
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
    lineShort: {
      height: 14,
      width: 96,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    lineTitle: {
      height: 18,
      width: "75%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    lineBody: {
      height: 12,
      width: "100%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    footer: {
      marginTop: "auto",
      flexDirection: "row",
      justifyContent: "space-between",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 10,
    },
    footerLine: {
      height: 12,
      width: 72,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
  };
}

export function SavedPostCardSkeleton() {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.card}>
      <View style={styles.thumb} />
      <View style={styles.body}>
        <View style={styles.row}>
          <View style={styles.avatar} />
          <View style={styles.lineShort} />
        </View>
        <View style={styles.lineTitle} />
        <View style={styles.lineBody} />
        <View style={styles.lineBody} />
        <View style={styles.footer}>
          <View style={styles.footerLine} />
          <View style={styles.footerLine} />
        </View>
      </View>
    </View>
  );
}
