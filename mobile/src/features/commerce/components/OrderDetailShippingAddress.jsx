import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    section: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    titleRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      marginBottom: 12,
    },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    name: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    address: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 6, lineHeight: 20 },
    phoneRow: { flexDirection: "row", alignItems: "center", gap: 6, marginTop: 10 },
    phone: { fontSize: 14, color: colors.onSurfaceVariant },
  };
}

export function OrderDetailShippingAddress({ address }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!address) return null;

  return (
    <View style={styles.section}>
      <View style={styles.titleRow}>
        <Text style={styles.title}>Địa chỉ giao hàng</Text>
        <Ionicons name="location-outline" size={20} color={colors.onSurfaceVariant} />
      </View>
      <Text style={styles.name}>{address.receiverName}</Text>
      <Text style={styles.address}>{address.fullAddress || address.addressDetail}</Text>
      {address.phone ? (
        <View style={styles.phoneRow}>
          <Ionicons name="call-outline" size={16} color={colors.onSurfaceVariant} />
          <Text style={styles.phone}>{address.phone}</Text>
        </View>
      ) : null}
    </View>
  );
}