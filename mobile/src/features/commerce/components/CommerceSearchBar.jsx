import { useEffect, useRef, useState } from "react";
import { Pressable, Text, TextInput, View } from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { MIN_KEYWORD_LENGTH, SEARCH_DEBOUNCE_MS } from "../constants/productSearchConstants";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      alignItems: "center",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 12,
      backgroundColor: colors.surface,
      overflow: "hidden",
    },
    input: {
      flex: 1,
      paddingHorizontal: 16,
      paddingVertical: 12,
      fontSize: 15,
      color: colors.onSurface,
    },
    button: {
      margin: 6,
      paddingHorizontal: 14,
      paddingVertical: 8,
      borderRadius: 8,
      backgroundColor: colors.primary,
    },
    buttonText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
  };
}

function resolveRawQuery(rawQuery) {
  if (typeof rawQuery === "string") return rawQuery;
  if (Array.isArray(rawQuery)) return rawQuery[0] ?? "";
  return "";
}

export function CommerceSearchBar({ onInvalidKeyword, style }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { q: rawQueryParam } = useLocalSearchParams();
  const urlQ = normalizeSearchKeyword(resolveRawQuery(rawQueryParam));
  const [inputValue, setInputValue] = useState(urlQ);
  const debounceRef = useRef(null);

  useEffect(() => {
    setInputValue(urlQ);
  }, [urlQ]);

  useEffect(() => {
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, []);

  const navigateWithKeyword = (raw) => {
    const normalized = normalizeSearchKeyword(raw);
    if (!normalized) {
      router.replace({ pathname: ROUTES.commerceSearch, params: {} });
      return;
    }
    if (normalized.length < MIN_KEYWORD_LENGTH) {
      onInvalidKeyword?.();
      return;
    }
    router.replace({ pathname: ROUTES.commerceSearch, params: { q: normalized } });
  };

  const handleChange = (next) => {
    setInputValue(next);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      const normalized = normalizeSearchKeyword(next);
      if (normalized.length >= MIN_KEYWORD_LENGTH) {
        router.replace({ pathname: ROUTES.commerceSearch, params: { q: normalized } });
      }
    }, SEARCH_DEBOUNCE_MS);
  };

  const handleSubmit = () => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    navigateWithKeyword(inputValue);
  };

  return (
    <View style={[styles.row, style]}>
      <TextInput
        value={inputValue}
        onChangeText={handleChange}
        placeholder="Tìm sản phẩm, thương hiệu hoặc danh mục..."
        placeholderTextColor={colors.onSurfaceVariant}
        style={styles.input}
        returnKeyType="search"
        onSubmitEditing={handleSubmit}
        accessibilityLabel="Tìm kiếm sản phẩm"
      />
      <Pressable style={styles.button} onPress={handleSubmit} accessibilityRole="button">
        <Text style={styles.buttonText}>Tìm kiếm</Text>
      </Pressable>
    </View>
  );
}
