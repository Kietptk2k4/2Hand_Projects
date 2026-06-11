import { StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams } from "expo-router";
import { colors } from "../../src/shared/theme/colors";

export default function HashtagPlaceholderScreen() {
  const { hashtag } = useLocalSearchParams();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>#{hashtag}</Text>
      <Text style={styles.hint}>Phase 7: hashtag feed.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    backgroundColor: colors.surface,
    justifyContent: "center",
    alignItems: "center",
  },
  title: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
    marginBottom: 16,
  },
  hint: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
});
