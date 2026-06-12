import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatOrderDate } from "../utils/formatOrderDate";

function createStyles(colors) {
  return {
    section: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface, marginBottom: 16 },
    empty: { fontSize: 14, color: colors.onSurfaceVariant },
    timeline: { paddingLeft: 4 },
    line: {
      position: "absolute",
      left: 15,
      top: 8,
      bottom: 24,
      width: 2,
      backgroundColor: colors.outlineVariant,
    },
    events: { gap: 20 },
    eventRow: { flexDirection: "row", gap: 12 },
    dot: {
      width: 24,
      height: 24,
      borderRadius: 12,
      alignItems: "center",
      justifyContent: "center",
      borderWidth: 2,
      borderColor: colors.surfaceContainerLowest,
      marginTop: 2,
    },
    dotLatest: { backgroundColor: colors.primary },
    dotPast: { backgroundColor: `${colors.primary}CC` },
    eventLabel: { fontSize: 14, color: colors.onSurface },
    eventLabelLatest: { fontWeight: "600" },
    eventTime: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 2 },
  };
}

export function OrderDetailTimeline({ events, isLoading }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Tiến trình đơn hàng</Text>

      {isLoading && !events?.length ? (
        <Text style={styles.empty}>Đang tải tiến trình...</Text>
      ) : null}

      {!isLoading && !events?.length ? (
        <Text style={styles.empty}>Chưa có sự kiện theo dõi.</Text>
      ) : null}

      {events?.length ? (
        <View style={styles.timeline}>
          <View style={styles.line} />
          <View style={styles.events}>
            {events.map((event) => (
              <View key={event.id} style={styles.eventRow}>
                <View style={[styles.dot, event.isLatest ? styles.dotLatest : styles.dotPast]}>
                  <Ionicons name="checkmark" size={14} color={colors.onPrimary} />
                </View>
                <View style={{ flex: 1 }}>
                  <Text
                    style={[styles.eventLabel, event.isLatest ? styles.eventLabelLatest : null]}
                  >
                    {event.label}
                  </Text>
                  <Text style={styles.eventTime}>{formatOrderDate(event.occurredAt)}</Text>
                </View>
              </View>
            ))}
          </View>
        </View>
      ) : null}
    </View>
  );
}