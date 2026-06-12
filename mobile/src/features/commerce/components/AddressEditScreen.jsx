import { useCallback, useMemo } from "react";
import { ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Text, View } from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ADDRESS_TOAST_MESSAGES } from "../constants/addressConstants";
import { useAddresses } from "../hooks/useAddresses";
import { useUpdateAddress } from "../hooks/useUpdateAddress";
import { AddressForm } from "./AddressForm";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, gap: 12, paddingBottom: 32 },
    title: { fontSize: 20, fontWeight: "700", color: colors.onSurface },
    loading: { flex: 1, alignItems: "center", justifyContent: "center", padding: 24 },
    errorText: { fontSize: 14, color: colors.onErrorContainer, textAlign: "center" },
  };
}

export function AddressEditScreen() {
  const { addressId } = useLocalSearchParams();
  const styles = useThemedStyles(createStyles);
  const colors = useThemeColors();
  const { showToast } = useSocialToast();
  const { addresses, isLoading, errorMessage } = useAddresses();
  const { updateAddress, isUpdating } = useUpdateAddress();

  const address = useMemo(
    () => addresses.find((item) => String(item.id) === String(addressId)),
    [addressId, addresses]
  );

  const handleSubmit = useCallback(
    async (form) => {
      await updateAddress({ addressId: address.id, form });
      showToast(ADDRESS_TOAST_MESSAGES.updateSuccess);
      router.back();
    },
    [address?.id, showToast, updateAddress]
  );

  if (isLoading && !address) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }

  if (!address) {
    return (
      <View style={styles.loading}>
        <Text style={styles.errorText}>{errorMessage || "Không tìm thấy địa chỉ."}</Text>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>Sửa địa chỉ</Text>
        <AddressForm
          initialAddress={address}
          isSubmitting={isUpdating}
          onSubmit={handleSubmit}
          submitLabel="Lưu thay đổi"
        />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}