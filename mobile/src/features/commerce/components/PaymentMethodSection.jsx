import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { PAYMENT_METHODS } from "../constants/checkoutConstants";

function createStyles(colors) {
  return {
    section: { gap: 12 },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    option: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 14,
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
    },
    optionSelected: { borderColor: colors.primary, backgroundColor: colors.primaryContainer },
    radio: {
      width: 20,
      height: 20,
      borderRadius: 10,
      borderWidth: 2,
      borderColor: colors.outline,
      alignItems: "center",
      justifyContent: "center",
    },
    radioSelected: { borderColor: colors.primary },
    radioDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: colors.primary },
    label: { flex: 1, fontSize: 15, color: colors.onSurface },
    icon: { marginLeft: "auto" },
  };
}

export function PaymentMethodSection({ paymentMethod, onSelect, disabled }) {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Phương thức thanh toán</Text>
      {PAYMENT_METHODS.map((method) => {
        const selected = paymentMethod === method.value;
        return (
          <Pressable
            key={method.value}
            style={[styles.option, selected ? styles.optionSelected : null]}
            disabled={disabled}
            onPress={() => onSelect?.(method.value)}
          >
            <View style={[styles.radio, selected ? styles.radioSelected : null]}>
              {selected ? <View style={styles.radioDot} /> : null}
            </View>
            <Text style={styles.label}>{method.label}</Text>
            <Ionicons
              name={method.value === "COD" ? "cash-outline" : "card-outline"}
              size={20}
              color={selected ? "#0050cb" : "#666"}
              style={styles.icon}
            />
          </Pressable>
        );
      })}
    </View>
  );
}