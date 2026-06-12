import { Pressable, Text } from "react-native";
import { formatSocialCount } from "../utils/formatSocialCount";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    capsule: {
      minWidth: 48,
      height: 32,
      borderRadius: 16,
      paddingHorizontal: 12,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: colors.surfaceContainerLow,
      borderWidth: 1,
      borderColor: colors.primary,
    },
    capsuleCompact: {
      minWidth: 44,
      height: 28,
      paddingHorizontal: 10,
    },
    text: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.primary,
    },
    textCompact: {
      fontSize: 12,
    },
  };
}

export function LikeCountButton({
  count,
  onPress,
  size = "default",
  showZero = false,
}) {
  const styles = useThemedStyles(createStyles);
  const num = Number(count) || 0;

  if (num <= 0 && !showZero) return null;

  const isCompact = size === "compact";

  return (
    <Pressable
      onPress={(event) => {
        event?.stopPropagation?.();
        onPress?.(num);
      }}
      style={[styles.capsule, isCompact && styles.capsuleCompact]}
      accessibilityRole="button"
      accessibilityLabel={`Xem ${num} nguoi da thich`}
    >
      <Text style={[styles.text, isCompact && styles.textCompact]}>
        {formatSocialCount(num)}
      </Text>
    </Pressable>
  );
}
