import { Pressable, Text, View } from "react-native";
import { FEED_TAB_OPTIONS } from "../constants/feedTabs";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: {
      flexDirection: "row",
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    tab: {
      flex: 1,
      paddingVertical: 14,
      alignItems: "center",
      justifyContent: "center",
    },
    tabActive: {
      borderBottomWidth: 2,
      borderBottomColor: colors.primary,
      backgroundColor: colors.surfaceContainerLow,
    },
    tabText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurfaceVariant,
    },
    tabTextActive: {
      color: colors.primary,
      fontWeight: "600",
    },
  };
}

export function FeedTabs({ activeTab, onChange }) {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      {FEED_TAB_OPTIONS.map((tab) => {
        const isActive = tab.id === activeTab;
        return (
          <Pressable
            key={tab.id}
            style={[styles.tab, isActive && styles.tabActive]}
            onPress={() => onChange(tab.id)}
            accessibilityRole="tab"
            accessibilityState={{ selected: isActive }}
          >
            <Text style={[styles.tabText, isActive && styles.tabTextActive]}>{tab.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}
