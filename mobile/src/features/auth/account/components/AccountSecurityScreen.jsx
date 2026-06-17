import { router } from "expo-router";
import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { getLoginHistory, getLoginSessions, logoutAllSessions } from "../../api/authApi";
import { AccountConfirmModal } from "./AccountConfirmModal";
import { AccountCard } from "./AccountCard";
import { AccountInfoSkeleton } from "./AccountInfoSkeleton";
import { getSessionStatusLabel, LOGIN_METHOD_LABELS } from "../../constants/authUiStrings";
import { ROUTES } from "../../../../shared/constants/routes";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { clearAuthSession } from "../../utils/clearAuthSession";
import { setLoginBannerMessage, setSessionExpiredMessage } from "../../utils/authNavigationState";
import { formatAccountDateTime } from "../utils/formatAccountDateTime";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";

const PAGE_LIMIT = 20;
const LOGOUT_ALL_SUCCESS_MESSAGE = "Da dang xuat tat ca phien dang nhap.";

function SessionRow({ session, colors }) {
  return (
    <View style={{ gap: 8, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: colors.outlineVariant }}>
      <View style={{ flexDirection: "row", justifyContent: "space-between", gap: 8 }}>
        <Text style={{ fontWeight: "600", color: colors.onSurface, flex: 1 }}>
          {session.device_id || "Thiet bi khong xac dinh"}
        </Text>
        <Text style={{ fontSize: 12, color: "#166534", fontWeight: "600" }}>
          {getSessionStatusLabel(session.status)}
        </Text>
      </View>
      <Text style={{ fontSize: 13, color: colors.onSurfaceVariant }}>IP: {session.ip_address || "—"}</Text>
      <Text style={{ fontSize: 13, color: colors.onSurfaceVariant }}>
        Tao luc: {formatAccountDateTime(session.created_at)}
      </Text>
      <Text style={{ fontSize: 13, color: colors.onSurfaceVariant }}>
        Het han: {formatAccountDateTime(session.expires_at)}
      </Text>
      {session.user_agent ? (
        <Text style={{ fontSize: 12, color: colors.onSurfaceVariant }}>{session.user_agent}</Text>
      ) : null}
    </View>
  );
}

function HistoryRow({ item, colors }) {
  const success = Boolean(item.success);
  return (
    <View style={{ gap: 6, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: colors.outlineVariant }}>
      <View style={{ flexDirection: "row", gap: 8, alignItems: "center" }}>
        <Text style={{ fontWeight: "600", color: colors.onSurface }}>
          {LOGIN_METHOD_LABELS[item.login_method] || item.login_method || "Dang nhap"}
        </Text>
        <Text style={{ fontSize: 12, fontWeight: "600", color: success ? "#166534" : "#b3261e" }}>
          {success ? "Thanh cong" : "That bai"}
        </Text>
      </View>
      <Text style={{ fontSize: 13, color: colors.onSurfaceVariant }}>
        {formatAccountDateTime(item.created_at)}
      </Text>
      <Text style={{ fontSize: 13, color: colors.onSurfaceVariant }}>IP: {item.ip_address || "—"}</Text>
      {item.user_agent ? (
        <Text style={{ fontSize: 12, color: colors.onSurfaceVariant }}>{item.user_agent}</Text>
      ) : null}
    </View>
  );
}

function LoginSessionsPanel({ colors }) {
  const [sessions, setSessions] = useState([]);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [logoutAllError, setLogoutAllError] = useState("");
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [isLoggingOutAll, setIsLoggingOutAll] = useState(false);

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await getLoginSessions();
      setSessions(data?.sessions || []);
      setStatus("ready");
    } catch (error) {
      const handled = await handleAccountQueryError(error);
      if (handled) return;
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc danh sach phien dang nhap.");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onConfirmLogoutAll = async () => {
    setIsLoggingOutAll(true);
    setLogoutAllError("");
    try {
      await logoutAllSessions();
      setLoginBannerMessage(LOGOUT_ALL_SUCCESS_MESSAGE);
      await clearAuthSession({ redirectToLogin: false });
      router.replace(ROUTES.login);
    } catch (error) {
      if (error?.code === 401) {
        setSessionExpiredMessage(error?.message);
        await clearAuthSession({ redirectToLogin: false });
        router.replace(ROUTES.sessionExpired);
        setIsConfirmOpen(false);
        return;
      }
      setLogoutAllError(error?.message || "Co loi xay ra. Vui long thu lai.");
      setIsConfirmOpen(false);
    } finally {
      setIsLoggingOutAll(false);
    }
  };

  if (status === "loading") return <AccountInfoSkeleton />;
  if (status === "error") {
    return (
      <AccountCard>
        <Text style={{ color: colors.onSurfaceVariant, marginBottom: 12 }}>{errorMessage}</Text>
        <Pressable
          onPress={load}
          style={{ backgroundColor: colors.primary, borderRadius: 8, padding: 12, alignItems: "center" }}
        >
          <Text style={{ color: colors.onPrimary, fontWeight: "600" }}>Thu lai</Text>
        </Pressable>
      </AccountCard>
    );
  }

  return (
    <>
      {logoutAllError ? (
        <AccountCard>
          <Text style={{ color: colors.error, marginBottom: 8 }}>{logoutAllError}</Text>
          <Pressable onPress={() => setLogoutAllError("")}>
            <Text style={{ color: colors.primary, fontWeight: "600" }}>Thu lai</Text>
          </Pressable>
        </AccountCard>
      ) : null}

      <AccountCard title="Dang xuat tat ca">
        <Text style={{ fontSize: 14, color: colors.onSurfaceVariant, marginBottom: 12 }}>
          Dang xuat khoi tat ca thiet bi dang dang nhap. Ban se can dang nhap lai.
        </Text>
        <Pressable
          onPress={() => setIsConfirmOpen(true)}
          disabled={isLoggingOutAll}
          style={{
            borderWidth: 1,
            borderColor: colors.error,
            borderRadius: 8,
            padding: 12,
            alignItems: "center",
            opacity: isLoggingOutAll ? 0.6 : 1,
          }}
        >
          <Text style={{ color: colors.error, fontWeight: "600" }}>Dang xuat tat ca thiet bi</Text>
        </Pressable>
      </AccountCard>

      <AccountCard title="Phien dang nhap">
        {sessions.length === 0 ? (
          <Text style={{ color: colors.onSurfaceVariant }}>Khong co phien dang nhap dang hoat dong.</Text>
        ) : (
          sessions.map((session) => (
            <SessionRow key={session.id} session={session} colors={colors} />
          ))
        )}
      </AccountCard>

      <AccountConfirmModal
        visible={isConfirmOpen}
        title="Dang xuat tat ca thiet bi?"
        message="Ban se can dang nhap lai tren cac thiet bi khac."
        confirmLabel={isLoggingOutAll ? "Dang dang xuat..." : "Dang xuat"}
        onConfirm={onConfirmLogoutAll}
        onCancel={() => !isLoggingOutAll && setIsConfirmOpen(false)}
        isLoading={isLoggingOutAll}
        danger
        icon="!"
      />
    </>
  );
}

function LoginHistoryPanel({ colors }) {
  const [items, setItems] = useState([]);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [status, setStatus] = useState("loading");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchPage = useCallback(async (pageOffset, append = false) => {
    if (!append) setStatus("loading");
    else setLoadMoreStatus("loading");
    setErrorMessage("");

    try {
      const data = await getLoginHistory({ limit: PAGE_LIMIT, offset: pageOffset });
      const nextItems = data?.items || [];
      setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
      setOffset(pageOffset);
      setHasMore(nextItems.length === PAGE_LIMIT);
      setStatus("ready");
    } catch (error) {
      const handled = await handleAccountQueryError(error);
      if (handled) return;
      if (!append) setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc lich su dang nhap.");
    } finally {
      setLoadMoreStatus("idle");
    }
  }, []);

  useEffect(() => {
    fetchPage(0, false);
  }, [fetchPage]);

  if (status === "loading") return <AccountInfoSkeleton />;
  if (status === "error") {
    return (
      <AccountCard>
        <Text style={{ color: colors.onSurfaceVariant, marginBottom: 12 }}>{errorMessage}</Text>
        <Pressable
          onPress={() => fetchPage(0, false)}
          style={{ backgroundColor: colors.primary, borderRadius: 8, padding: 12, alignItems: "center" }}
        >
          <Text style={{ color: colors.onPrimary, fontWeight: "600" }}>Thu lai</Text>
        </Pressable>
      </AccountCard>
    );
  }

  return (
    <AccountCard title="Lich su dang nhap">
      {items.length === 0 ? (
        <Text style={{ color: colors.onSurfaceVariant }}>Chua co lich su dang nhap.</Text>
      ) : (
        <>
          {items.map((item) => (
            <HistoryRow key={item.id} item={item} colors={colors} />
          ))}
          {hasMore ? (
            <Pressable
              onPress={() => {
                if (loadMoreStatus === "loading") return;
                fetchPage(offset + PAGE_LIMIT, true);
              }}
              style={{
                marginTop: 12,
                borderWidth: 1,
                borderColor: colors.outlineVariant,
                borderRadius: 8,
                padding: 12,
                alignItems: "center",
              }}
            >
              {loadMoreStatus === "loading" ? (
                <ActivityIndicator color={colors.primary} />
              ) : (
                <Text style={{ color: colors.primary, fontWeight: "600" }}>Tai them</Text>
              )}
            </Pressable>
          ) : null}
        </>
      )}
      {errorMessage && status === "ready" ? (
        <Text style={{ marginTop: 8, color: colors.error }}>{errorMessage}</Text>
      ) : null}
    </AccountCard>
  );
}

export function AccountSecurityScreen() {
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();
  const [activeTab, setActiveTab] = useState("sessions");

  const styles = StyleSheet.create({
    screen: { flex: 1, backgroundColor: colors.surface },
    content: { paddingHorizontal: 16, paddingTop: 16, gap: 16 },
    headerTitle: { fontSize: 20, fontWeight: "600", color: colors.onSurface },
    headerSubtitle: { fontSize: 14, lineHeight: 20, color: colors.onSurfaceVariant, marginTop: 4 },
    tabs: { flexDirection: "row", gap: 8 },
    tab: {
      flex: 1,
      borderRadius: 8,
      paddingVertical: 10,
      alignItems: "center",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
    },
    tabActive: { backgroundColor: colors.primaryContainer, borderColor: colors.primary },
    tabText: { fontSize: 13, fontWeight: "600", color: colors.onSurfaceVariant },
    tabTextActive: { color: colors.primary },
  });

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
    >
      <View>
        <Text style={styles.headerTitle}>Bao mat tai khoan</Text>
        <Text style={styles.headerSubtitle}>
          Xem phien dang nhap dang hoat dong va lich su dang nhap cua tai khoan.
        </Text>
      </View>

      <View style={styles.tabs}>
        <Pressable
          style={[styles.tab, activeTab === "sessions" && styles.tabActive]}
          onPress={() => setActiveTab("sessions")}
        >
          <Text style={[styles.tabText, activeTab === "sessions" && styles.tabTextActive]}>
            Phien dang nhap
          </Text>
        </Pressable>
        <Pressable
          style={[styles.tab, activeTab === "history" && styles.tabActive]}
          onPress={() => setActiveTab("history")}
        >
          <Text style={[styles.tabText, activeTab === "history" && styles.tabTextActive]}>
            Lich su
          </Text>
        </Pressable>
      </View>

      {activeTab === "sessions" ? (
        <LoginSessionsPanel colors={colors} />
      ) : (
        <LoginHistoryPanel colors={colors} />
      )}
    </ScrollView>
  );
}
