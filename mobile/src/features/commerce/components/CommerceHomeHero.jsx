import { useState } from "react";
import { Pressable, Text, TextInput, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    section: {
      marginBottom: 24,
      alignItems: "center",
      gap: 12,
    },
    title: {
      fontSize: 24,
      fontWeight: "700",
      color: colors.onSurface,
      textAlign: "center",
    },
    subtitle: {
      fontSize: 15,
      lineHeight: 22,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      paddingHorizontal: 8,
    },
    searchRow: {
      width: "100%",
      flexDirection: "row",
      alignItems: "center",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 12,
      backgroundColor: colors.surface,
      overflow: "hidden",
    },
    searchInput: {
      flex: 1,
      paddingHorizontal: 16,
      paddingVertical: 14,
      fontSize: 15,
      color: colors.onSurface,
    },
    searchButton: {
      margin: 6,
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 8,
      backgroundColor: colors.primary,
    },
    searchButtonText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
    chips: {
      flexDirection: "row",
      flexWrap: "wrap",
      justifyContent: "center",
      gap: 8,
      marginTop: 8,
    },
    chip: {
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 999,
      paddingHorizontal: 14,
      paddingVertical: 8,
      backgroundColor: colors.surfaceContainerLowest,
    },
    chipText: {
      fontSize: 14,
      color: colors.onSurface,
    },
    loadingText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
  };
}

export function CommerceHomeHero({
  onSearchSubmit,
  onCategoryClick,
  navItems = [],
  isLoadingNav = false,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [query, setQuery] = useState("");

  const handleSubmit = () => {
    onSearchSubmit?.(query);
  };

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Khám phá sản phẩm chuyên nghiệp</Text>
      <Text style={styles.subtitle}>
        Tìm công cụ và vật tư chất lượng từ các shop đã xác minh trên 2Hands.
      </Text>

      <View style={styles.searchRow}>
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Tìm sản phẩm, thương hiệu hoặc danh mục..."
          placeholderTextColor={colors.onSurfaceVariant}
          style={styles.searchInput}
          returnKeyType="search"
          onSubmitEditing={handleSubmit}
          accessibilityLabel="Tìm kiếm sản phẩm"
        />
        <Pressable style={styles.searchButton} onPress={handleSubmit} accessibilityRole="button">
          <Text style={styles.searchButtonText}>Tìm kiếm</Text>
        </Pressable>
      </View>

      <View style={styles.chips}>
        {isLoadingNav ? <Text style={styles.loadingText}>Đang tải danh mục...</Text> : null}
        {navItems.map((item) => (
          <Pressable
            key={item.categoryId}
            style={styles.chip}
            onPress={() => onCategoryClick?.(item)}
            accessibilityRole="button"
          >
            <Text style={styles.chipText}>{item.label}</Text>
          </Pressable>
        ))}
      </View>
    </View>
  );
}
