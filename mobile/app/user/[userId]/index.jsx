import { StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams } from "expo-router";
import { colors } from "../../../src/shared/theme/colors";

export default function UserProfilePlaceholderScreen() {
  const { userId } = useLocalSearchParams();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ho so nguoi dung</Text>
      <Text style={styles.subtitle}>User ID: {userId}</Text>
      <Text style={styles.hint}>Phase 6: profile day du.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    backgroundColor: colors.surface,
    justifyContent: "center",
  },
  title: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    marginBottom: 16,
  },
  hint: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
});
