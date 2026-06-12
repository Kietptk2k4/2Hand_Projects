import { View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      gap: 12,
      marginBottom: 16,
    },
    avatar: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
    },
    body: {
      flex: 1,
      gap: 8,
    },
    lineShort: {
      width: "33%",
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerHigh,
    },
    lineLong: {
      width: "100%",
      height: 56,
      borderRadius: 8,
      backgroundColor: colors.surfaceContainerHigh,
    },
  };
}

export function CommentSkeleton() {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.row}>
      <View style={styles.avatar} />
      <View style={styles.body}>
        <View style={styles.lineShort} />
        <View style={styles.lineLong} />
      </View>
    </View>
  );
}
