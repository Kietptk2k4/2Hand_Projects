import { View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function StarRating({ rating = 0, size = 18 }) {
  const colors = useThemeColors();
  const rounded = Math.round(rating);

  return (
    <View style={{ flexDirection: "row", gap: 2 }} accessibilityRole="image">
      {[1, 2, 3, 4, 5].map((star) => (
        <Ionicons
          key={star}
          name={star <= rounded ? "star" : "star-outline"}
          size={size}
          color={star <= rounded ? colors.primary : colors.outlineVariant}
        />
      ))}
    </View>
  );
}
