import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 20,
      alignItems: "center",
      gap: 8,
    },
    avg: { fontSize: 36, fontWeight: "700", color: colors.onSurface },
    stars: { flexDirection: "row", gap: 4 },
    count: { fontSize: 14, color: colors.onSurfaceVariant },
  };
}

function StarRow({ rating, size = 22 }) {
  const colors = useThemeColors();
  const rounded = Math.round(rating);

  return (
    <View style={{ flexDirection: "row", gap: 2 }}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Ionicons
          key={star}
          name={star <= rounded ? "star" : "star-outline"}
          size={size}
          color={star <= rounded ? "#F59E0B" : colors.outlineVariant}
        />
      ))}
    </View>
  );
}

export function ProductReviewsSummary({ ratingSummary }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  const avg = ratingSummary?.ratingAvg ?? 0;
  const count = ratingSummary?.ratingCount ?? 0;

  return (
    <View style={styles.card}>
      <Text style={styles.avg}>{avg > 0 ? avg.toFixed(1) : "—"}</Text>
      <View style={styles.stars}>
        <StarRow rating={avg} size={22} />
      </View>
      <Text style={styles.count}>{count} đánh giá</Text>
    </View>
  );
}
