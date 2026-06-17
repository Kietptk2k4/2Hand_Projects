import { useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Text, View } from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const FALLBACK_COUNTDOWN_SEC = 5;

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface, padding: 16 },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 32,
      alignItems: "center",
      gap: 12,
    },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface, textAlign: "center" },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    meta: { fontSize: 12, color: colors.onSurfaceVariant, textAlign: "center" },
  };
}

function resolveParam(raw) {
  if (typeof raw === "string") return raw.trim();
  if (Array.isArray(raw)) return String(raw[0] || "").trim();
  return "";
}

export function CommerceVnpayReturnScreen() {
  const styles = useThemedStyles(createStyles);
  const params = useLocalSearchParams();
  const [countdown, setCountdown] = useState(FALLBACK_COUNTDOWN_SEC);

  const status = resolveParam(params.status);
  const orderId = resolveParam(params.orderId);

  const statusLabel = useMemo(() => {
    if (status === "success") return "success";
    if (status === "failed") return "failed";
    return status || "—";
  }, [status]);

  useEffect(() => {
    if (status === "success") {
      router.replace({
        pathname: ROUTES.commerceOrders,
        params: { status: "PROCESSING" },
      });
      return;
    }
    if (status === "failed") {
      router.replace({
        pathname: ROUTES.commerceOrders,
        params: { status: "AWAITING_PAYMENT" },
      });
      return;
    }

    const timer = setInterval(() => {
      setCountdown((value) => Math.max(0, value - 1));
    }, 1000);

    return () => clearInterval(timer);
  }, [status]);

  useEffect(() => {
    if (status) return;
    if (countdown === 0) {
      router.replace(ROUTES.commerceOrders);
    }
  }, [countdown, status]);

  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <ActivityIndicator size="large" />
        <Text style={styles.title}>Đang xử lý kết quả thanh toán</Text>
        <Text style={styles.subtitle}>Đang điều hướng…</Text>
        {orderId ? <Text style={styles.meta}>OrderId: {orderId}</Text> : null}
        <Text style={styles.meta}>Status: {statusLabel}</Text>
        {!status ? (
          <Text style={styles.meta}>Tự chuyển về danh sách đơn hàng sau {countdown}s…</Text>
        ) : null}
      </View>
    </View>
  );
}
