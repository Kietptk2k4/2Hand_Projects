import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { colors } from "../theme/colors";

const SocialToastContext = createContext(null);

export function SocialToastProvider({ children }) {
  const [toast, setToast] = useState(null);

  const showToast = useCallback((message, variant = "info") => {
    if (!message) return;
    setToast({ message, variant });
  }, []);

  useEffect(() => {
    if (!toast) return undefined;
    const timer = setTimeout(() => setToast(null), 2800);
    return () => clearTimeout(timer);
  }, [toast]);

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <SocialToastContext.Provider value={value}>
      {children}
      {toast ? (
        <View
          pointerEvents="none"
          style={[
            styles.banner,
            toast.variant === "error" ? styles.bannerError : styles.bannerInfo,
          ]}
        >
          <Text
            style={[
              styles.bannerText,
              toast.variant === "error" ? styles.bannerTextError : styles.bannerTextInfo,
            ]}
          >
            {toast.message}
          </Text>
        </View>
      ) : null}
    </SocialToastContext.Provider>
  );
}

export function useSocialToast() {
  const context = useContext(SocialToastContext);
  if (!context) {
    throw new Error("useSocialToast must be used within SocialToastProvider");
  }
  return context;
}

const styles = StyleSheet.create({
  banner: {
    position: "absolute",
    left: 16,
    right: 16,
    bottom: 24,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    elevation: 4,
  },
  bannerInfo: {
    backgroundColor: colors.surfaceContainerLowest,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
  },
  bannerError: {
    backgroundColor: colors.errorContainer,
    borderWidth: 1,
    borderColor: colors.error,
  },
  bannerText: {
    fontSize: 14,
    lineHeight: 20,
    textAlign: "center",
  },
  bannerTextInfo: {
    color: colors.onSurface,
  },
  bannerTextError: {
    color: colors.onErrorContainer,
  },
});
