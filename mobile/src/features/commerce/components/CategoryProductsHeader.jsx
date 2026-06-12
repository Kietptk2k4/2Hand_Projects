import { Switch, Text, View } from "react-native";
import { CATEGORY_DESCRIPTIONS } from "../constants/categoryProductsConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ProductListSortSelect } from "./ProductListSortSelect";

function createStyles(colors) {
  return {
    header: {
      gap: 12,
      marginBottom: 8,
    },
    title: {
      fontSize: 24,
      fontWeight: "700",
      color: colors.onSurface,
    },
    description: {
      fontSize: 14,
      lineHeight: 20,
      color: colors.onSurfaceVariant,
    },
    count: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
    },
    switchRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
    },
    switchLabel: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
      flex: 1,
    },
    sortRow: {
      alignItems: "flex-start",
    },
  };
}

export function CategoryProductsHeader({
  categoryName,
  categorySlug,
  totalItems,
  sort,
  onSortChange,
  includeChildren,
  onIncludeChildrenChange,
  sortDisabled = false,
}) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const description =
    CATEGORY_DESCRIPTIONS[categorySlug] ||
    "Khám phá sản phẩm chất lượng từ các shop đã xác minh trên 2Hands.";

  return (
    <View style={styles.header}>
      <Text style={styles.title}>{categoryName}</Text>
      <Text style={styles.description}>{description}</Text>
      {totalItems != null ? <Text style={styles.count}>{totalItems} sản phẩm</Text> : null}

      <View style={styles.switchRow}>
        <Switch
          value={includeChildren}
          onValueChange={onIncludeChildrenChange}
          accessibilityLabel="Bao gồm danh mục con"
        />
        <Text style={styles.switchLabel}>Bao gồm danh mục con</Text>
      </View>

      <View style={styles.sortRow}>
        <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
      </View>
    </View>
  );
}
