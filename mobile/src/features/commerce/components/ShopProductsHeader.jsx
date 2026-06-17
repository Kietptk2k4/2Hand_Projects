import { Text, View } from "react-native";
import { ProductListSortSelect } from "./ProductListSortSelect";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    header: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      flexWrap: "wrap",
      marginBottom: 16,
    },
    title: {
      flex: 1,
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
    count: {
      fontWeight: "400",
      color: colors.onSurfaceVariant,
    },
  };
}

export function ShopProductsHeader({ totalItems, sort, onSortChange, sortDisabled = false }) {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.header}>
      <Text style={styles.title}>
        Sản phẩm của shop
        {totalItems != null ? <Text style={styles.count}> ({totalItems})</Text> : null}
      </Text>
      <ProductListSortSelect value={sort} onChange={onSortChange} disabled={sortDisabled} />
    </View>
  );
}
