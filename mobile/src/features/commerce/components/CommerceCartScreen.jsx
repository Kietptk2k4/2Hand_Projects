import { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, ScrollView, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useCart } from "../hooks/useCart";
import { useValidateCartItems } from "../hooks/useValidateCartItems";
import {
  getCartItemCountLabel,
  getEligibleCartItems,
  getSelectedEligibleCartItems,
  isCartItemCheckoutEligible,
  isCartItemInvalid,
} from "../utils/cartDisplay";
import { CartEmptyState } from "./CartEmptyState";
import { CartInvalidItemsBanner } from "./CartInvalidItemsBanner";
import { CartItemRow } from "./CartItemRow";
import { CartSkeleton } from "./CartSkeleton";
import { CartSummaryCard } from "./CartSummaryCard";
import { CartWarningsBanner } from "./CartWarningsBanner";

const CHECKOUT_BLOCKED_TOAST = "Không thể thanh toán. Vui lòng kiểm tra lại giỏ hàng.";
const CHECKOUT_NO_SELECTION_TOAST = "Vui lòng chọn ít nhất một sản phẩm để thanh toán.";

function createStyles(colors) {
  return {
    container: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    content: {
      padding: 16,
      gap: 16,
      paddingBottom: 32,
    },
    subtitle: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    errorCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    errorText: {
      fontSize: 14,
      color: colors.onErrorContainer,
      textAlign: "center",
    },
    retryButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    retryText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
    selectAllRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    checkbox: {
      width: 20,
      height: 20,
      borderRadius: 4,
      borderWidth: 1.5,
      borderColor: colors.outlineVariant,
      alignItems: "center",
      justifyContent: "center",
    },
    checkboxChecked: {
      backgroundColor: colors.primary,
      borderColor: colors.primary,
    },
    checkboxDisabled: {
      opacity: 0.4,
    },
    selectAllText: {
      fontSize: 14,
      color: colors.onSurface,
    },
    items: {
      gap: 12,
    },
  };
}

function SelectAllCheckbox({ checked, disabled, onPress, styles, colors }) {
  return (
    <Pressable
      style={[
        styles.checkbox,
        checked ? styles.checkboxChecked : null,
        disabled ? styles.checkboxDisabled : null,
      ]}
      disabled={disabled}
      onPress={onPress}
      accessibilityRole="checkbox"
      accessibilityState={{ checked, disabled }}
      accessibilityLabel="Chọn tất cả sản phẩm có thể thanh toán"
    >
      {checked ? <Ionicons name="checkmark" size={14} color={colors.onPrimary} /> : null}
    </Pressable>
  );
}

export function CommerceCartScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [selectedIds, setSelectedIds] = useState(() => new Set());
  const { validate, isValidating } = useValidateCartItems();
  const {
    cart,
    isLoading,
    isEmpty,
    errorMessage,
    isMutating,
    mutatingItemId,
    updateQuantity,
    removeItem,
    retry,
    revalidate,
  } = useCart();

  const eligibleCartItems = useMemo(
    () => (cart ? getEligibleCartItems(cart) : []),
    [cart]
  );

  const eligibleIds = useMemo(
    () => eligibleCartItems.map((item) => item.cartItemId),
    [eligibleCartItems]
  );

  const cartItemSignature = useMemo(
    () =>
      cart?.items
        ?.map(
          (item) =>
            `${item.cartItemId}:${item.quantity}:${item.status}:${item.unavailableReason ?? ""}`
        )
        .join("|") ?? "",
    [cart?.items]
  );

  useEffect(() => {
    if (!cart?.items?.length) {
      setSelectedIds(new Set());
      return;
    }

    setSelectedIds((prev) => {
      const eligibleSet = new Set(eligibleIds);
      const kept = [...prev].filter((id) => eligibleSet.has(id));
      if (kept.length > 0) return new Set(kept);
      return new Set(eligibleIds);
    });
  }, [cart?.items?.length, cartItemSignature, eligibleIds]);

  const selectedEligibleItems = useMemo(
    () => getSelectedEligibleCartItems(cart, selectedIds),
    [cart, selectedIds]
  );

  const allEligibleSelected =
    eligibleIds.length > 0 && eligibleIds.every((id) => selectedIds.has(id));

  const toggleSelectItem = useCallback((cartItemId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(cartItemId)) {
        next.delete(cartItemId);
      } else {
        next.add(cartItemId);
      }
      return next;
    });
  }, []);

  const toggleSelectAllEligible = useCallback(() => {
    setSelectedIds(() => {
      if (allEligibleSelected) return new Set();
      return new Set(eligibleIds);
    });
  }, [allEligibleSelected, eligibleIds]);

  const goToCheckout = useCallback(async () => {
    if (!cart) return;

    if (!selectedEligibleItems.length) {
      showToast(CHECKOUT_NO_SELECTION_TOAST, "error");
      return;
    }

    setIsCheckingOut(true);
    try {
      const checkoutIds = selectedEligibleItems.map((item) => item.cartItemId);
      const result = await validate(checkoutIds);
      if (!result?.canCheckout) {
        await revalidate(checkoutIds);
        showToast(CHECKOUT_BLOCKED_TOAST, "error");
        return;
      }

      const validIds = result.validItems.map((entry) => entry.cartItemId);
      router.push({
        pathname: ROUTES.commerceCheckout,
        params: {
          cartItemIds: validIds.join(","),
        },
      });
    } catch {
      showToast(CHECKOUT_BLOCKED_TOAST, "error");
    } finally {
      setIsCheckingOut(false);
    }
  }, [cart, revalidate, selectedEligibleItems, showToast, validate]);

  const openProduct = useCallback((productId) => {
    if (!productId) return;
    router.push(ROUTES.commerceProductDetail(productId));
  }, []);

  const handleDecrease = useCallback(
    (item) => {
      if (isCartItemInvalid(item)) return;
      updateQuantity(item.cartItemId, item.quantity - 1);
    },
    [updateQuantity]
  );

  const handleIncrease = useCallback(
    (item) => {
      if (isCartItemInvalid(item)) return;
      if (item.quantity >= item.availableQuantity) return;
      updateQuantity(item.cartItemId, item.quantity + 1);
    },
    [updateQuantity]
  );

  const canCheckout = Boolean(selectedEligibleItems.length > 0 && !isValidating);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {cart ? <Text style={styles.subtitle}>{getCartItemCountLabel(cart)}</Text> : null}

      {isLoading ? <CartSkeleton /> : null}

      {!isLoading && errorMessage ? (
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={() => retry()}>
            <Text style={styles.retryText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? <CartEmptyState /> : null}

      {!isLoading && !errorMessage && cart && !isEmpty ? (
        <>
          <CartWarningsBanner warnings={cart.summary.warnings} />
          <CartInvalidItemsBanner items={cart.items} />

          <View style={styles.selectAllRow}>
            <SelectAllCheckbox
              checked={allEligibleSelected}
              disabled={eligibleIds.length === 0 || isMutating}
              onPress={toggleSelectAllEligible}
              styles={styles}
              colors={colors}
            />
            <Text style={styles.selectAllText}>
              Chọn tất cả ({eligibleIds.length} sản phẩm)
            </Text>
          </View>

          <View style={styles.items}>
            {cart.items.map((item) => (
              <CartItemRow
                key={item.cartItemId}
                item={item}
                selected={selectedIds.has(item.cartItemId)}
                canSelect={isCartItemCheckoutEligible(item)}
                isMutating={isMutating && mutatingItemId === item.cartItemId}
                onToggleSelect={toggleSelectItem}
                onOpenProduct={openProduct}
                onRemove={removeItem}
                onDecrease={() => handleDecrease(item)}
                onIncrease={() => handleIncrease(item)}
              />
            ))}
          </View>

          <CartSummaryCard
            cart={cart}
            selectedItems={selectedEligibleItems}
            isMutating={isMutating || isCheckingOut || isValidating}
            canCheckout={canCheckout}
            onCheckout={goToCheckout}
          />
        </>
      ) : null}
    </ScrollView>
  );
}
