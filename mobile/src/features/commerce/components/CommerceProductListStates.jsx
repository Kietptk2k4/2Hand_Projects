import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    errorCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.error,
      backgroundColor: colors.errorContainer,
      padding: 20,
      alignItems: "center",
      gap: 12,
    },
    errorText: {
      fontSize: 14,
      color: colors.onErrorContainer,
      textAlign: "center",
    },
    emptyCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
    },
    emptyText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    retryButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      backgroundColor: colors.primary,
    },
    retryText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
    footer: {
      paddingVertical: 20,
      alignItems: "center",
    },
  };
}

export function CommerceProductListError({ message, onRetry }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  if (!message) return null;

  return (
    <View style={styles.errorCard}>
      <Text style={styles.errorText}>{message}</Text>
      {onRetry ? (
        <Pressable style={styles.retryButton} onPress={onRetry}>
          <Text style={styles.retryText}>Thử lại</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function CommerceProductListEmpty({ message = "Chưa có sản phẩm nào để hiển thị." }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.emptyCard}>
      <Text style={styles.emptyText}>{message}</Text>
    </View>
  );
}

export function CommerceProductListFooter({ isLoadingMore, hasNext, onLoadMore, colors: palette }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!hasNext) return null;

  return (
    <View style={styles.footer}>
      {isLoadingMore ? (
        <ActivityIndicator color={palette?.primary} />
      ) : (
        <Pressable style={styles.retryButton} onPress={onLoadMore}>
          <Text style={styles.retryText}>Tải thêm sản phẩm</Text>
        </Pressable>
      )}
    </View>
  );
}
