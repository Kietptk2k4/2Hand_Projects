import { ActivityIndicator, Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { DEFAULT_SHIPMENT_LABEL } from "../constants/checkoutConstants";
import { formatVndPrice } from "../utils/formatVndPrice";

function createStyles(colors) {
  return {
    section: { gap: 8 },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 14,
      gap: 8,
    },
    row: { flexDirection: "row", justifyContent: "space-between", gap: 12 },
    label: { fontSize: 14, color: colors.onSurfaceVariant },
    value: { fontSize: 14, color: colors.onSurface, fontWeight: "500" },
    loading: { paddingVertical: 8, alignItems: "center" },
    hint: { fontSize: 13, color: colors.onSurfaceVariant },
  };
}

export function OrderSummarySection({ quote, shippingFee, isLoading, selectedAddressId }) {
  const styles = useThemedStyles(createStyles);
  const colors = useThemeColors();

  if (!selectedAddressId) {
    return (
      <View style={styles.section}>
        <Text style={styles.title}>Vận chuyển</Text>
        <Text style={styles.hint}>Chọn địa chỉ giao hàng để xem phí vận chuyển.</Text>
      </View>
    );
  }

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Vận chuyển</Text>
      <View style={styles.card}>
        <View style={styles.row}>
          <Text style={styles.label}>Hình thức</Text>
          <Text style={styles.value}>{DEFAULT_SHIPMENT_LABEL}</Text>
        </View>
        {isLoading ? (
          <View style={styles.loading}>
            <ActivityIndicator color={colors.primary} size="small" />
          </View>
        ) : (
          <>
            <View style={styles.row}>
              <Text style={styles.label}>Phí vận chuyển</Text>
              <Text style={styles.value}>
                {formatVndPrice(quote?.shippingFee ?? shippingFee?.totalShippingFee ?? 0)}
              </Text>
            </View>
            {shippingFee?.sellerGroups?.[0]?.estimatedDeliveryDate ? (
              <Text style={styles.hint}>
                Dự kiến giao: {shippingFee.sellerGroups[0].estimatedDeliveryDate}
              </Text>
            ) : null}
          </>
        )}
      </View>
    </View>
  );
}