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

export function SavedPostsHeader() {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.header}>
      <Text style={styles.title}>Đã lưu</Text>
      <Text style={styles.subtitle}>
        Quản lý các bài viết dịch vụ bạn đã đánh dấu.
      </Text>
    </View>
  );
}
