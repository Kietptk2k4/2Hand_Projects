import { useCallback } from "react";
import { KeyboardAvoidingView, Platform, ScrollView, Text, View } from "react-native";
import { router } from "expo-router";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ADDRESS_TOAST_MESSAGES } from "../constants/addressConstants";
import { useCreateAddress } from "../hooks/useCreateAddress";
import { AddressForm } from "./AddressForm";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, gap: 12, paddingBottom: 32 },
    title: { fontSize: 20, fontWeight: "700", color: colors.onSurface },
  };
}

export function AddressCreateScreen() {
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { createAddress, isCreating } = useCreateAddress();

  const handleSubmit = useCallback(
    async (form) => {
      await createAddress(form);
      showToast(ADDRESS_TOAST_MESSAGES.createSuccess);
      router.back();
    },
    [createAddress, showToast]
  );

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>Thêm địa chỉ mới</Text>
        <AddressForm isSubmitting={isCreating} onSubmit={handleSubmit} submitLabel="Lưu địa chỉ" />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}