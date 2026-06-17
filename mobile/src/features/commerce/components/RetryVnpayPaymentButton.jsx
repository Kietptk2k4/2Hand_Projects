import { Pressable, Text, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useVnpayRetry } from "../hooks/useVnpayRetry";

function createStyles(colors) {
  return {
    wrap: { alignItems: "flex-end", gap: 4 },
    button: {
      borderRadius: 10,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    errorText: { fontSize: 12, color: colors.error },
  };
}

export function RetryVnpayPaymentButton({
  orderId,
  label = "Thanh toán lại",
  onPress,
  style,
  textStyle,
}) {
  const styles = useThemedStyles(createStyles);
  const { retry, isRetrying, error } = useVnpayRetry(orderId);

  const handlePress = async () => {
    onPress?.();
    await retry();
  };

  return (
    <View style={styles.wrap}>
      <Pressable
        style={[styles.button, isRetrying ? styles.buttonDisabled : null, style]}
        onPress={handlePress}
        disabled={isRetrying}
        accessibilityRole="button"
      >
        <Text style={[styles.buttonText, textStyle]}>
          {isRetrying ? "Đang tạo liên kết..." : label}
        </Text>
      </Pressable>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
    </View>
  );
}
