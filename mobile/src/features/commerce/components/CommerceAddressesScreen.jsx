import { useCallback, useState } from "react";
import { Alert, Pressable, ScrollView, Text, View } from "react-native";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ADDRESS_TOAST_MESSAGES } from "../constants/addressConstants";
import { useAddresses } from "../hooks/useAddresses";
import { useDeleteAddress } from "../hooks/useDeleteAddress";
import { useSetDefaultAddress } from "../hooks/useSetDefaultAddress";
import { AddressCard } from "./AddressCard";
import { AddressEmptyState } from "./AddressEmptyState";
import { AddressListSkeleton } from "./AddressListSkeleton";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, gap: 16, paddingBottom: 32 },
    header: { gap: 8 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, lineHeight: 20 },
    addButton: {
      alignSelf: "flex-start",
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    addButtonDisabled: { opacity: 0.6 },
    addButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
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
    list: { gap: 12 },
  };
}

export function CommerceAddressesScreen() {
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { addresses, labelVersion, isLoading, errorMessage, isEmpty, retry } = useAddresses();
  const { deleteAddress, isDeleting } = useDeleteAddress();
  const { setDefaultAddress, isSettingDefault } = useSetDefaultAddress();
  const [mutatingAddressId, setMutatingAddressId] = useState(null);

  const isMutating = isDeleting || isSettingDefault || Boolean(mutatingAddressId);

  const openCreate = useCallback(() => {
    router.push(ROUTES.commerceAddressCreate);
  }, []);

  const openEdit = useCallback((address) => {
    router.push(ROUTES.commerceAddressEdit(address.id));
  }, []);

  const handleSetDefault = useCallback(
    async (address) => {
      setMutatingAddressId(address.id);
      try {
        await setDefaultAddress(address.id);
        showToast(ADDRESS_TOAST_MESSAGES.setDefaultSuccess);
        await retry();
      } catch {
        showToast("Không thể đặt địa chỉ mặc định.", "error");
      } finally {
        setMutatingAddressId(null);
      }
    },
    [retry, setDefaultAddress, showToast]
  );

  const handleDelete = useCallback(
    (address) => {
      Alert.alert(
        "Xóa địa chỉ",
        `Bạn có chắc muốn xóa địa chỉ của ${address.receiverName}?`,
        [
          { text: "Hủy", style: "cancel" },
          {
            text: "Xóa",
            style: "destructive",
            onPress: async () => {
              setMutatingAddressId(address.id);
              try {
                await deleteAddress(address.id);
                showToast(ADDRESS_TOAST_MESSAGES.deleteSuccess);
                await retry();
              } catch {
                showToast("Không thể xóa địa chỉ.", "error");
              } finally {
                setMutatingAddressId(null);
              }
            },
          },
        ]
      );
    },
    [deleteAddress, retry, showToast]
  );

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>Sổ địa chỉ giao hàng</Text>
        <Text style={styles.subtitle}>
          Quản lý địa chỉ nhận hàng khi mua sắm. Đơn đã đặt vẫn giữ địa chỉ tại thời điểm thanh toán.
        </Text>
        <Pressable
          style={[styles.addButton, isMutating ? styles.addButtonDisabled : null]}
          disabled={isMutating}
          onPress={openCreate}
        >
          <Text style={styles.addButtonText}>Thêm địa chỉ</Text>
        </Pressable>
      </View>

      {isLoading ? <AddressListSkeleton /> : null}

      {!isLoading && errorMessage ? (
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={() => retry()}>
            <Text style={styles.retryText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? <AddressEmptyState onAdd={openCreate} /> : null}

      {!isLoading && !errorMessage && addresses.length > 0 ? (
        <View style={styles.list}>
          {addresses.map((address) => (
            <AddressCard
              key={`${address.id}-${labelVersion}`}
              address={address}
              disabled={isMutating}
              isMutating={mutatingAddressId === address.id}
              onEdit={openEdit}
              onSetDefault={handleSetDefault}
              onDelete={handleDelete}
            />
          ))}
        </View>
      ) : null}
    </ScrollView>
  );
}