import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatAddressHeader, formatAddressLine } from "../utils/formatAddressLine";

function createStyles(colors) {
  return {
    section: { gap: 12 },
    sectionTitle: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 14,
      gap: 8,
    },
    cardSelected: { borderColor: colors.primary, backgroundColor: colors.primaryContainer },
    row: { flexDirection: "row", alignItems: "flex-start", gap: 12 },
    radio: {
      width: 20,
      height: 20,
      borderRadius: 10,
      borderWidth: 2,
      borderColor: colors.outline,
      alignItems: "center",
      justifyContent: "center",
      marginTop: 2,
    },
    radioSelected: { borderColor: colors.primary },
    radioDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: colors.primary },
    body: { flex: 1, gap: 4 },
    header: { fontSize: 15, fontWeight: "600", color: colors.onSurface },
    line: { fontSize: 14, color: colors.onSurfaceVariant, lineHeight: 20 },
    linkRow: { flexDirection: "row", flexWrap: "wrap", gap: 12 },
    link: { fontSize: 14, fontWeight: "600", color: colors.primary },
    empty: { fontSize: 14, color: colors.onSurfaceVariant },
  };
}

export function AddressPicker({ addresses, selectedAddressId, onSelect, disabled }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!addresses.length) {
    return (
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Địa chỉ giao hàng</Text>
        <Text style={styles.empty}>Bạn chưa có địa chỉ giao hàng.</Text>
        <Pressable onPress={() => router.push(ROUTES.commerceAddressCreate)} disabled={disabled}>
          <Text style={styles.link}>Thêm địa chỉ</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>Địa chỉ giao hàng</Text>
      {addresses.map((address) => {
        const selected = address.id === selectedAddressId;
        return (
          <Pressable
            key={address.id}
            style={[styles.card, selected ? styles.cardSelected : null]}
            disabled={disabled}
            onPress={() => onSelect?.(address.id)}
          >
            <View style={styles.row}>
              <View style={[styles.radio, selected ? styles.radioSelected : null]}>
                {selected ? <View style={styles.radioDot} /> : null}
              </View>
              <View style={styles.body}>
                <Text style={styles.header}>{formatAddressHeader(address)}</Text>
                <Text style={styles.line}>{formatAddressLine(address)}</Text>
              </View>
            </View>
          </Pressable>
        );
      })}
      <View style={styles.linkRow}>
        <Pressable onPress={() => router.push(ROUTES.commerceAddressCreate)} disabled={disabled}>
          <Text style={styles.link}>Thêm địa chỉ mới</Text>
        </Pressable>
        <Pressable onPress={() => router.push(ROUTES.commerceAddresses)} disabled={disabled}>
          <Text style={styles.link}>Quản lý địa chỉ</Text>
        </Pressable>
      </View>
    </View>
  );
}