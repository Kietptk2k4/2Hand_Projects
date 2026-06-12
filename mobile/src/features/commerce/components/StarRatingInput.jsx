import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    row: { flexDirection: "row", gap: 8 },
    starButton: { padding: 4 },
    hint: { marginTop: 6, fontSize: 13, color: colors.onSurfaceVariant },
  };
}

export function StarRatingInput({ value = 0, onChange, disabled = false, size = 32 }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View>
      <View style={styles.row} accessibilityRole="radiogroup" accessibilityLabel="Chọn số sao đánh giá">
        {[1, 2, 3, 4, 5].map((star) => {
          const filled = star <= value;
          return (
            <Pressable
              key={star}
              style={styles.starButton}
              disabled={disabled}
              onPress={() => onChange?.(star)}
              accessibilityRole="radio"
              accessibilityState={{ checked: value === star }}
              accessibilityLabel={`${star} sao`}
            >
              <Ionicons
                name={filled ? "star" : "star-outline"}
                size={size}
                color={filled ? "#F59E0B" : colors.outlineVariant}
              />
            </Pressable>
          );
        })}
      </View>
      <Text style={styles.hint}>{value ? `Bạn chọn ${value} sao` : "Chạm để chọn số sao"}</Text>
    </View>
  );
}
