import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      flexDirection: "row",
      alignItems: "center",
      overflow: "hidden",
      borderRadius: 999,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surface,
    },
    rootDisabled: {
      opacity: 0.5,
    },
    button: {
      paddingHorizontal: 12,
      paddingVertical: 6,
      alignItems: "center",
      justifyContent: "center",
    },
    quantity: {
      width: 32,
      textAlign: "center",
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurface,
    },
  };
}

export function CartQuantityStepper({
  quantity,
  disabled = false,
  isLoading = false,
  maxQuantity,
  onDecrease,
  onIncrease,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const atMin = quantity <= 1;
  const atMax = maxQuantity != null && quantity >= maxQuantity;
  const isDisabled = disabled || isLoading;

  return (
    <View style={[styles.root, isDisabled ? styles.rootDisabled : null]}>
      <Pressable
        style={styles.button}
        accessibilityLabel="Giảm số lượng"
        disabled={isDisabled || atMin}
        onPress={onDecrease}
      >
        {isLoading ? (
          <ActivityIndicator size="small" color={colors.onSurfaceVariant} />
        ) : (
          <Ionicons
            name="remove"
            size={16}
            color={isDisabled || atMin ? colors.outline : colors.onSurfaceVariant}
          />
        )}
      </Pressable>
      <Text style={styles.quantity}>{quantity}</Text>
      <Pressable
        style={styles.button}
        accessibilityLabel="Tăng số lượng"
        disabled={isDisabled || atMax}
        onPress={onIncrease}
      >
        <Ionicons
          name="add"
          size={16}
          color={isDisabled || atMax ? colors.outline : colors.onSurfaceVariant}
        />
      </Pressable>
    </View>
  );
}
