import { Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const VARIANT_KEYS = {
  sale: "sale",
  lowStock: "lowStock",
  soldOut: "soldOut",
  condition: "condition",
};

function createStyles(colors) {
  const base = {
    badge: {
      alignSelf: "flex-start",
      borderWidth: 2,
      borderRadius: 4,
      paddingHorizontal: 8,
      paddingVertical: 2,
      shadowColor: "#000",
      shadowOffset: { width: 2, height: 3 },
      shadowOpacity: 0.22,
      shadowRadius: 0,
      elevation: 2,
    },
    text: {
      fontSize: 10,
      fontWeight: "700",
      textTransform: "uppercase",
      letterSpacing: 0.5,
    },
  };

  return {
    ...base,
    sale: {
      badge: { ...base.badge, backgroundColor: "#E53935", borderColor: "rgba(255,255,255,0.9)" },
      text: { ...base.text, color: "#FFFFFF" },
    },
    lowStock: {
      badge: { ...base.badge, backgroundColor: "#F59E0B", borderColor: "rgba(255,255,255,0.9)" },
      text: { ...base.text, color: "#FFFFFF" },
    },
    soldOut: {
      badge: { ...base.badge, backgroundColor: "rgba(38,38,38,0.95)", borderColor: "rgba(255,255,255,0.25)" },
      text: { ...base.text, color: "#FFFFFF" },
    },
    condition: {
      badge: { ...base.badge, backgroundColor: colors.primary, borderColor: "rgba(255,255,255,0.9)" },
      text: { ...base.text, color: colors.onPrimary },
    },
  };
}

export function ProductStickerBadge({ children, variant = "sale", style }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const key = VARIANT_KEYS[variant] ?? VARIANT_KEYS.sale;
  const variantStyles = styles[key];

  return (
    <View style={[variantStyles.badge, style]} pointerEvents="none">
      <Text style={variantStyles.text}>{children}</Text>
    </View>
  );
}
