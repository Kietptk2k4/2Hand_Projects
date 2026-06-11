import { ActivityIndicator, StyleSheet, View } from "react-native";
import { ProfileScreen } from "../../src/features/social/components/ProfileScreen";
import { useCurrentUserId } from "../../src/features/social/hooks/useCurrentUserId";
import { colors } from "../../src/shared/theme/colors";

export default function ProfileTabScreen() {
  const currentUserId = useCurrentUserId();

  if (!currentUserId) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return <ProfileScreen userId={currentUserId} />;
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surface,
  },
});
