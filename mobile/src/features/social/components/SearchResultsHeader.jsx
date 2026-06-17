import { Text, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    header: {
      paddingTop: 4,
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

export function SearchResultsHeader({ keyword, totalElements = 0 }) {
  const styles = useThemedStyles(createStyles);
  const trimmed = String(keyword || "").trim();

  if (!trimmed) {
    return (
      <View style={styles.header}>
        <Text style={styles.title}>Tìm kiếm bài viết</Text>
        <Text style={styles.subtitle}>Nhập từ khóa để tìm bài viết</Text>
      </View>
    );
  }

  return (
    <View style={styles.header}>
      <Text style={styles.title}>Kết quả cho "{trimmed}"</Text>
      <Text style={styles.subtitle}>{totalElements} kết quả</Text>
    </View>
  );
}
