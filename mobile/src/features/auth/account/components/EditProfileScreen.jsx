import { useCallback } from "react";
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { colors } from "../../../../shared/theme/colors";
import { useEditProfile } from "../hooks/useEditProfile";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { EditProfileForm } from "./EditProfileForm";

export function EditProfileScreen() {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(() => {
    showToast("Cập nhật hồ sơ thành công.");
  }, [showToast]);

  const onError = useCallback(
    (message) => {
      showToast(message, "error");
    },
    [showToast]
  );

  const editProfile = useEditProfile({ onSuccess, onError });

  if (editProfile.isLoading) {
    return (
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      >
        <AccountInfoSkeleton />
      </ScrollView>
    );
  }

  if (editProfile.isProfileError) {
    return (
      <View style={[styles.centered, { paddingBottom: insets.bottom }]}>
        <Text style={styles.errorText}>{editProfile.profileErrorMessage}</Text>
        <Pressable style={styles.retryButton} onPress={editProfile.retryProfile}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.screen}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
      keyboardVerticalOffset={Platform.OS === "ios" ? 8 : 0}
    >
      <ScrollView
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
        keyboardShouldPersistTaps="handled"
      >
        <EditProfileForm {...editProfile} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 16,
  },
  centered: {
    flex: 1,
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
    gap: 16,
  },
  errorText: {
    fontSize: 16,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  retryButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    minHeight: 48,
    minWidth: 120,
    paddingHorizontal: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  retryButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
});