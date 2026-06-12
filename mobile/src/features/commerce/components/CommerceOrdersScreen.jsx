import { useCallback } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  Text,
  View,
} from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useOrderList } from "../hooks/useOrderList";
import { OrderListCard } from "./OrderListCard";
import { OrderListEmptyState } from "./OrderListEmptyState";
import { OrderListFilters } from "./OrderListFilters";
import { OrderListSkeleton } from "./OrderListSkeleton";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, paddingBottom: 32 },
    header: { gap: 4, marginBottom: 12 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant },
    subtitleCount: { color: colors.onSurface },
    filtersWrap: { marginBottom: 16 },
    list: { gap: 12 },
    errorCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    errorText: { fontSize: 14, color: colors.onErrorContainer, textAlign: "center" },
    retryButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    retryText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    footer: { paddingVertical: 20, alignItems: "center", gap: 12 },
    loadMoreBtn: {
      borderRadius: 10,
      borderWidth: 2,
      borderColor: colors.primary,
      paddingHorizontal: 24,
      paddingVertical: 12,
    },
    loadMoreText: { fontSize: 14, fontWeight: "700", color: colors.primary },
  };
}

export function CommerceOrdersScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  const {
    orders,
    activeFilterId,
    changeStatusFilter,
    isInitialLoading,
    isLoadingMore,
    isEmpty,
    hasNext,
    totalItems,
    errorMessage,
    loadMore,
    retry,
  } = useOrderList();

  const handleFilterChange = useCallback(
    (nextStatus) => {
      changeStatusFilter(nextStatus);
    },
    [changeStatusFilter],
  );

  const renderItem = useCallback(
    ({ item }) => <OrderListCard order={item} />,
    [],
  );

  const keyExtractor = useCallback((item) => item.orderId, []);

  const ListHeader = (
    <View>
      <View style={styles.header}>
        <Text style={styles.title}>Đơn hàng của tôi</Text>
        <Text style={styles.subtitle}>
          Theo dõi và quản lý đơn hàng
          {totalItems > 0 ? (
            <Text style={styles.subtitleCount}> · {totalItems} đơn</Text>
          ) : null}
        </Text>
      </View>
      <View style={styles.filtersWrap}>
        <OrderListFilters
          activeFilterId={activeFilterId}
          onChange={handleFilterChange}
          disabled={isInitialLoading}
        />
      </View>
      {isInitialLoading ? <OrderListSkeleton /> : null}
    </View>
  );

  const ListEmpty = !isInitialLoading && !errorMessage && isEmpty ? <OrderListEmptyState /> : null;

  const ListError =
    !isInitialLoading && errorMessage && orders.length === 0 ? (
      <View style={styles.errorCard}>
        <Text style={styles.errorText}>{errorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={retry}>
          <Text style={styles.retryText}>Thử lại</Text>
        </Pressable>
      </View>
    ) : null;

  const ListFooter =
    hasNext && !isInitialLoading && !errorMessage ? (
      <View style={styles.footer}>
        {isLoadingMore ? (
          <ActivityIndicator color={colors.primary} accessibilityLabel="Đang tải thêm" />
        ) : (
          <Pressable style={styles.loadMoreBtn} onPress={loadMore}>
            <Text style={styles.loadMoreText}>Xem thêm</Text>
          </Pressable>
        )}
      </View>
    ) : null;

  if (isInitialLoading) {
    return (
      <View style={styles.container}>
        <View style={styles.content}>{ListHeader}</View>
      </View>
    );
  }

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={orders}
      keyExtractor={keyExtractor}
      renderItem={renderItem}
      ListHeaderComponent={ListHeader}
      ListEmptyComponent={ListEmpty || ListError}
      ListFooterComponent={ListFooter}
      onEndReached={loadMore}
      onEndReachedThreshold={0.4}
      ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
    />
  );
}