import { useCallback, useEffect, useMemo, useState } from "react";
import { ScrollView, View } from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { createPayOsCheckoutUrl, retryVnpayPayment } from "../api/paymentApi";
import { canRetryPayment } from "../constants/paymentStatusLabels";
import { usePaymentStatus } from "../hooks/usePaymentStatus";
import { openPayOsBrowser } from "../utils/openPayOsBrowser";
import { openVnpayBrowser } from "../utils/openVnpayBrowser";
import { mapPayOsCheckoutUrlResponse, mapVnpayRetryResponse } from "../utils/paymentMapper";
import { PaymentStatusPanel } from "./PaymentStatusPanel";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

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
    },
  };
}

export function CommercePaymentResultScreen() {
  const params = useLocalSearchParams();
  const paymentId = String(params.paymentId || "").trim();
  const mockPaid = params.mockPaid === "1";
  const mockFailed = params.mockFailed === "1";
  const mockExpired = params.mockExpired === "1";
  const [isRetrying, setIsRetrying] = useState(false);
  const styles = useThemedStyles(createStyles);

  const isValidPaymentId = useMemo(() => UUID_REGEX.test(paymentId), [paymentId]);

  useEffect(() => {
    if (!isValidPaymentId) {
      router.replace(ROUTES.commerceHome);
    }
  }, [isValidPaymentId]);

  const {
    status,
    orderId,
    amount,
    paidAt,
    expiredAt,
    orderStatus,
    orderPaymentStatus,
    payosCheckoutUrl,
    paymentMethod,
    isLoading,
    error,
    refresh,
  } = usePaymentStatus(isValidPaymentId ? paymentId : null, {
    poll: true,
    mockPaid,
    mockFailed,
    mockExpired,
  });

  const showRetry = useMemo(
    () => canRetryPayment({ paymentStatus: status, orderStatus }),
    [status, orderStatus]
  );

  const handleRetryPayment = useCallback(async () => {
    if (!paymentId) return;

    setIsRetrying(true);
    try {
      if (paymentMethod === "VNPAY") {
        if (!orderId) {
          await refresh();
          return;
        }
        const raw = await retryVnpayPayment(orderId);
        const redirect = mapVnpayRetryResponse(raw)?.redirect;
        if (redirect) {
          await openVnpayBrowser(redirect);
          return;
        }
        await refresh();
        return;
      }

      let url = payosCheckoutUrl;
      if (!url) {
        const raw = await createPayOsCheckoutUrl(paymentId);
        url = mapPayOsCheckoutUrlResponse(raw)?.payosCheckoutUrl;
      }
      if (url) {
        await openPayOsBrowser(url);
      } else {
        await refresh();
      }
    } catch {
      await refresh();
    } finally {
      setIsRetrying(false);
    }
  }, [orderId, paymentId, paymentMethod, payosCheckoutUrl, refresh]);

  if (!isValidPaymentId) {
    return null;
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.card}>
        <PaymentStatusPanel
          status={status}
          orderId={orderId}
          amount={amount}
          paidAt={paidAt}
          expiredAt={expiredAt}
          orderStatus={orderStatus}
          orderPaymentStatus={orderPaymentStatus}
          isLoading={isLoading}
          error={error}
          showRetry={showRetry}
          onRetryPayment={showRetry ? handleRetryPayment : undefined}
          isRetrying={isRetrying}
          paymentMethod={paymentMethod}
        />
      </View>
    </ScrollView>
  );
}