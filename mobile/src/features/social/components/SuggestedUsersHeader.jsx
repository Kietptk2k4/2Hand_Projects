import { Text, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    header: {
      paddingHorizontal: 16,
      paddingTop: 8,
      paddingBottom: 16,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      marginBottom: 8,
    },
    title: {
      fontSize: 24,
      fontWeight: "600",
      color: colors.onSurface,
    },
    subtitle: {
      marginTop: 4,
      fontSize: 15,
      lineHeight: 22,
      color: colors.onSurfaceVariant,
    },
  };
}

export function SuggestedUsersHeader() {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.header}>
      <Text style={styles.title}>Những người bạn có thể biết</Text>
      <Text style={styles.subtitle}>
        Gợi ý dựa trên bạn chung và mạng lưới của bạn trên 2Hands.
      </Text>
    </View>
  );
}
