import { useCallback, useEffect, useMemo } from "react";
import { Pressable, ScrollView, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { QUOTE_DISCLAIMER } from "../constants/checkoutConstants";
import { useCheckout } from "../hooks/useCheckout";
import { CheckoutSkeleton } from "./CheckoutSkeleton";
import { CheckoutSummaryCard } from "./CheckoutSummaryCard";
import { OrderSummarySection } from "./OrderSummarySection";
import { PaymentMethodSection } from "./PaymentMethodSection";
import { ShippingAddressSection } from "./ShippingAddressSection";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, gap: 16, paddingBottom: 32 },
    header: { gap: 4 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    secureRow: { flexDirection: "row", alignItems: "center", gap: 6 },
    secureText: { fontSize: 14, color: colors.onSurfaceVariant },
    backLink: { fontSize: 14, fontWeight: "600", color: colors.primary, marginTop: 4 },
    disclaimer: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.primary}33`,
      backgroundColor: colors.surfaceContainerLow,
      padding: 12,
    },
    disclaimerText: { fontSize: 13, color: colors.onSurface, lineHeight: 18 },
    errorCard: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 14,
      gap: 8,
    },
    errorText: { fontSize: 14, color: colors.onErrorContainer },
    retryText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    emptyCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 32,
      alignItems: "center",
      gap: 12,
    },
    emptyText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    primaryButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 20,
      paddingVertical: 12,
    },
    primaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
  };
}

export function CommerceCheckoutScreen() {
  const { cartItemIds: cartItemIdsParam } = useLocalSearchParams();
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  const cartItemIds = useMemo(() => {
    if (!cartItemIdsParam) return [];
    return String(cartItemIdsParam).split(",").map((id) => id.trim()).filter(Boolean);
  }, [cartItemIdsParam]);

  useEffect(() => {
    if (!cartItemIds.length) {
      router.replace(ROUTES.commerceCart);
    }
  }, [cartItemIds.length]);

  const {
    addresses,
    addressLabelVersion,
    selectedAddressId,
    paymentMethod,
    quote,
    shippingFee,
    quoteError,
    submitError,
    isSubmitting,
    isLoadingAddresses,
    isLoadingQuote,
    isEmptyAddresses,
    canSubmit,
    selectAddress,
    selectPayment,
    refreshQuote,
    submitOrder,
  } = useCheckout(cartItemIds);

  const handlePlaceOrder = useCallback(async () => {
    if (!canSubmit || isSubmitting) return;
    const result = await submitOrder();
    if (!result?.orderId) return;

    router.replace({
      pathname: "/commerce/checkout/success",
      params: {
        orderId: String(result.orderId),
        ...(result.paymentId ? { paymentId: String(result.paymentId) } : {}),
        ...(result.finalAmount != null ? { finalAmount: String(result.finalAmount) } : {}),
        paymentMethod: String(result.paymentMethod || paymentMethod),
        orderStatus: String(result.orderStatus || ""),
        paymentStatus: String(result.paymentStatus || ""),
      },
    });
  }, [canSubmit, isSubmitting, paymentMethod, submitOrder]);

  if (!cartItemIds.length) {
    return null;
  }

  const showSkeleton = isLoadingAddresses;
  const showCheckoutForm = !showSkeleton && !isEmptyAddresses;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>Thanh toÃ¡n an toÃ n</Text>
        <View style={styles.secureRow}>
          <Ionicons name="lock-closed" size={14} color={colors.primary} />
          <Text style={styles.secureText}>ThÃ´ng tin Ä‘Æ°á»£c báº£o máº­t</Text>
        </View>
        <Pressable onPress={() => router.push(ROUTES.commerceCart)}>
          <Text style={styles.backLink}>Quay láº¡i giá» hÃ ng</Text>
        </Pressable>
      </View>

      <View style={styles.disclaimer}>
        <Text style={styles.disclaimerText}>{QUOTE_DISCLAIMER}</Text>
      </View>

      {showSkeleton ? <CheckoutSkeleton /> : null}

      {!showSkeleton && isEmptyAddresses ? (
        <View style={styles.emptyCard}>
          <Ionicons name="location-outline" size={40} color={colors.outline} />
          <Text style={styles.emptyText}>Báº¡n chÆ°a cÃ³ Ä‘á»‹a chá»‰ giao hÃ ng.</Text>
          <Pressable style={styles.primaryButton} onPress={() => router.push(ROUTES.commerceAddressCreate)}>
            <Text style={styles.primaryButtonText}>ThÃªm Ä‘á»‹a chá»‰</Text>
          </Pressable>
          <Pressable onPress={() => router.push(ROUTES.commerceCart)}>
            <Text style={styles.backLink}>Quay láº¡i giá» hÃ ng</Text>
          </Pressable>
        </View>
      ) : null}

      {showCheckoutForm ? (
        <>
          <ShippingAddressSection
            key={addressLabelVersion}
            addresses={addresses}
            selectedAddressId={selectedAddressId}
            onSelect={selectAddress}
            disabled={isSubmitting}
          />

          <OrderSummarySection
            quote={quote}
            shippingFee={shippingFee}
            isLoading={isLoadingQuote}
            selectedAddressId={selectedAddressId}
          />

          <PaymentMethodSection
            paymentMethod={paymentMethod}
            onSelect={selectPayment}
            disabled={isSubmitting}
          />

          {quoteError ? (
            <View style={styles.errorCard}>
              <Text style={styles.errorText}>{quoteError}</Text>
              <Pressable onPress={refreshQuote}>
                <Text style={styles.retryText}>Thá»­ láº¡i</Text>
              </Pressable>
            </View>
          ) : null}

          {submitError ? (
            <View style={styles.errorCard}>
              <Text style={styles.errorText}>{submitError}</Text>
            </View>
          ) : null}

          <CheckoutSummaryCard
            quote={quote}
            isLoading={isLoadingQuote}
            canSubmit={canSubmit}
            isSubmitting={isSubmitting}
            onPlaceOrder={handlePlaceOrder}
          />
        </>
      ) : null}
    </ScrollView>
  );
}