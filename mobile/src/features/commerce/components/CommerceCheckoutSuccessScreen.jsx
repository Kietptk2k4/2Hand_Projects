import { useCallback, useEffect } from "react";
import { ActivityIndicator, Pressable, ScrollView, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { useQueryClient } from "@tanstack/react-query";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { cartKeys } from "../api/cartKeys";
import { PAYOS_AUTO_REDIRECT_MS } from "../constants/paymentConstants";
import { usePayOsCheckout } from "../hooks/usePayOsCheckout";
import { useVnpayCheckout } from "../hooks/useVnpayCheckout";
import { formatVndPrice } from "../utils/formatVndPrice";
import { openPayOsBrowser } from "../utils/openPayOsBrowser";
import { openVnpayBrowser } from "../utils/openVnpayBrowser";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, paddingBottom: 32 },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 8,
    },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface, textAlign: "center" },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    amount: { fontSize: 24, fontWeight: "700", color: colors.primary, marginTop: 8 },
    infoBox: {
      marginTop: 12,
      width: "100%",
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerLow,
      padding: 14,
      gap: 6,
    },
    infoText: { fontSize: 14, color: colors.onSurface, lineHeight: 20 },
    meta: { fontSize: 12, color: colors.onSurfaceVariant },
    actions: { marginTop: 16, width: "100%", gap: 10 },
    primaryButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingVertical: 12,
      alignItems: "center",
    },
    primaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    secondaryButton: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.primary,
      paddingVertical: 12,
      alignItems: "center",
    },
    secondaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    link: { fontSize: 14, color: colors.primary, textAlign: "center", marginTop: 8 },
    errorText: { fontSize: 14, color: colors.error, textAlign: "center" },
  };
}

function CodSuccessContent({ orderId, finalAmount, orderStatus, paymentStatus, styles }) {
  return (
    <>
      <Ionicons name="checkmark-circle" size={56} color="#0050cb" />
      <Text style={styles.title}>Đặt hàng thành công</Text>
      <Text style={styles.subtitle}>Mã đơn hàng: {orderId}</Text>
      {finalAmount != null && !Number.isNaN(finalAmount) ? (
        <Text style={styles.amount}>{formatVndPrice(finalAmount)}</Text>
      ) : null}
      <View style={styles.infoBox}>
        <Text style={styles.infoText}>Đơn hàng đang được xử lý. Bạn sẽ thanh toán khi nhận hàng.</Text>
        <Text style={styles.meta}>
          Trạng thái: {orderStatus || "—"} · Thanh toán: {paymentStatus || "—"}
        </Text>
      </View>
      <View style={styles.actions}>
        <Pressable style={styles.primaryButton} onPress={() => router.replace(ROUTES.commerceHome)}>
          <Text style={styles.primaryButtonText}>Tiếp tục mua sắm</Text>
        </Pressable>
        <Pressable style={styles.secondaryButton} onPress={() => router.push(ROUTES.commerceCart)}>
          <Text style={styles.secondaryButtonText}>Xem giỏ hàng</Text>
        </Pressable>
      </View>
    </>
  );
}

function PayOsSuccessContent({ orderId, paymentId, finalAmount, orderStatus, paymentStatus, styles, colors }) {
  const { checkoutUrl, isLoading, error, retry } = usePayOsCheckout(paymentId);

  const goToPayOs = useCallback(async () => {
    if (checkoutUrl) {
      await openPayOsBrowser(checkoutUrl);
    }
  }, [checkoutUrl]);

  useEffect(() => {
    if (!checkoutUrl || isLoading || error) return;

    const timer = setTimeout(() => {
      openPayOsBrowser(checkoutUrl);
    }, PAYOS_AUTO_REDIRECT_MS);

    return () => clearTimeout(timer);
  }, [checkoutUrl, isLoading, error]);

  return (
    <>
      <Ionicons name="card-outline" size={56} color="#d97706" />
      <Text style={styles.title}>Đặt hàng thành công</Text>
      <Text style={styles.subtitle}>Mã đơn hàng: {orderId}</Text>
      {finalAmount != null && !Number.isNaN(finalAmount) ? (
        <Text style={styles.amount}>{formatVndPrice(finalAmount)}</Text>
      ) : null}
      <View style={styles.infoBox}>
        <Text style={[styles.infoText, { fontWeight: "600" }]}>Chờ thanh toán qua PayOS</Text>
        <Text style={styles.infoText}>Vui lòng hoàn tất thanh toán để đơn hàng được xử lý.</Text>
        <Text style={styles.meta}>
          Trạng thái đơn: {orderStatus || "—"} · Thanh toán: {paymentStatus || "—"}
        </Text>
      </View>

      {isLoading ? (
        <View style={{ marginTop: 16, alignItems: "center", gap: 8 }}>
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.subtitle}>Đang tạo liên kết thanh toán...</Text>
        </View>
      ) : null}

      {error ? (
        <View style={{ marginTop: 16, width: "100%", gap: 10 }}>
          <Text style={styles.errorText}>{error}</Text>
          <Pressable style={styles.primaryButton} onPress={retry}>
            <Text style={styles.primaryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      {!isLoading && !error && checkoutUrl ? (
        <View style={{ marginTop: 16, width: "100%", gap: 10 }}>
          <Text style={styles.subtitle}>Bạn sẽ được chuyển tới PayOS trong giây lát...</Text>
          <Pressable style={styles.primaryButton} onPress={goToPayOs}>
            <Text style={styles.primaryButtonText}>Thanh toán ngay qua PayOS</Text>
          </Pressable>
        </View>
      ) : null}

      <Pressable onPress={() => router.replace(ROUTES.commerceHome)}>
        <Text style={styles.link}>Thanh toán sau</Text>
      </Pressable>
    </>
  );
}

function VnpaySuccessContent({ orderId, paymentId, finalAmount, orderStatus, paymentStatus, styles, colors }) {
  const { checkoutUrl, isLoading, error, retry } = useVnpayCheckout(paymentId);

  const goToVnpay = useCallback(async () => {
    if (checkoutUrl) {
      await openVnpayBrowser(checkoutUrl);
    }
  }, [checkoutUrl]);

  useEffect(() => {
    if (!checkoutUrl || isLoading || error) return;

    const timer = setTimeout(() => {
      openVnpayBrowser(checkoutUrl);
    }, PAYOS_AUTO_REDIRECT_MS);

    return () => clearTimeout(timer);
  }, [checkoutUrl, isLoading, error]);

  return (
    <>
      <Ionicons name="card-outline" size={56} color="#d97706" />
      <Text style={styles.title}>Đặt hàng thành công</Text>
      <Text style={styles.subtitle}>Mã đơn hàng: {orderId}</Text>
      {finalAmount != null && !Number.isNaN(finalAmount) ? (
        <Text style={styles.amount}>{formatVndPrice(finalAmount)}</Text>
      ) : null}
      <View style={styles.infoBox}>
        <Text style={[styles.infoText, { fontWeight: "600" }]}>Chờ thanh toán qua VNPay</Text>
        <Text style={styles.infoText}>Vui lòng hoàn tất thanh toán để đơn hàng được xử lý.</Text>
        <Text style={styles.meta}>
          Trạng thái đơn: {orderStatus || "—"} · Thanh toán: {paymentStatus || "—"}
        </Text>
      </View>

      {isLoading ? (
        <View style={{ marginTop: 16, alignItems: "center", gap: 8 }}>
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.subtitle}>Đang tạo liên kết thanh toán...</Text>
        </View>
      ) : null}

      {error ? (
        <View style={{ marginTop: 16, width: "100%", gap: 10 }}>
          <Text style={styles.errorText}>{error}</Text>
          <Pressable style={styles.primaryButton} onPress={retry}>
            <Text style={styles.primaryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      {!isLoading && !error && checkoutUrl ? (
        <View style={{ marginTop: 16, width: "100%", gap: 10 }}>
          <Text style={styles.subtitle}>Bạn sẽ được chuyển tới VNPay trong giây lát...</Text>
          <Pressable style={styles.primaryButton} onPress={goToVnpay}>
            <Text style={styles.primaryButtonText}>Thanh toán ngay qua VNPay</Text>
          </Pressable>
        </View>
      ) : null}

      <Pressable onPress={() => router.replace(ROUTES.commerceHome)}>
        <Text style={styles.link}>Thanh toán sau</Text>
      </Pressable>
    </>
  );
}

export function CommerceCheckoutSuccessScreen() {
  const params = useLocalSearchParams();
  const styles = useThemedStyles(createStyles);
  const colors = useThemeColors();
  const queryClient = useQueryClient();

  const orderId = String(params.orderId || "").trim();
  const paymentId = String(params.paymentId || "").trim();
  const paymentMethod = String(params.paymentMethod || "").trim();
  const orderStatus = String(params.orderStatus || "").trim();
  const paymentStatus = String(params.paymentStatus || "").trim();
  const finalAmountRaw = params.finalAmount != null ? Number(params.finalAmount) : null;
  const finalAmount = Number.isFinite(finalAmountRaw) ? finalAmountRaw : null;

  const isCod = paymentMethod === "COD";
  const isPayos = paymentMethod === "PAYOS";
  const isVnpay = paymentMethod === "VNPAY";

  useEffect(() => {
    if (orderId) {
      queryClient.invalidateQueries({ queryKey: cartKeys.detail() });
    }
  }, [orderId, queryClient]);

  useEffect(() => {
    if (!orderId) {
      router.replace(ROUTES.commerceHome);
      return;
    }
    if ((isPayos || isVnpay) && !paymentId) {
      router.replace(ROUTES.commerceCart);
    }
  }, [isPayos, isVnpay, orderId, paymentId]);

  if (!orderId) {
    return null;
  }

  if ((isPayos || isVnpay) && !paymentId) {
    return null;
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.card}>
        {isCod ? (
          <CodSuccessContent
            orderId={orderId}
            finalAmount={finalAmount}
            orderStatus={orderStatus}
            paymentStatus={paymentStatus}
            styles={styles}
          />
        ) : null}
        {isPayos ? (
          <PayOsSuccessContent
            orderId={orderId}
            paymentId={paymentId}
            finalAmount={finalAmount}
            orderStatus={orderStatus}
            paymentStatus={paymentStatus}
            styles={styles}
            colors={colors}
          />
        ) : null}
        {isVnpay ? (
          <VnpaySuccessContent
            orderId={orderId}
            paymentId={paymentId}
            finalAmount={finalAmount}
            orderStatus={orderStatus}
            paymentStatus={paymentStatus}
            styles={styles}
            colors={colors}
          />
        ) : null}
        {!isCod && !isPayos && !isVnpay ? (
          <CodSuccessContent
            orderId={orderId}
            finalAmount={finalAmount}
            orderStatus={orderStatus}
            paymentStatus={paymentStatus}
            styles={styles}
          />
        ) : null}
      </View>
    </ScrollView>
  );
}