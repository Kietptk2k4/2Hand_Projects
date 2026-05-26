import { useCallback, useEffect, useState } from "react";
import { getLoginHistory } from "../../api/authApi";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../shared/ui/PageState.jsx";

const PAGE_LIMIT = 20;

const LOGIN_METHOD_LABELS = {
  EMAIL: "Email",
  GOOGLE: "Google",
  FACEBOOK: "Facebook",
};

function HistoryRow({ item }) {
  const success = Boolean(item.success);

  return (
    <li className="flex flex-col gap-3 border-b border-outline-variant/50 py-4 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-on-surface">
            {LOGIN_METHOD_LABELS[item.login_method] || item.login_method || "Dang nhap"}
          </span>
          <span
            className={[
              "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold",
              success ? "bg-green-50 text-green-800" : "bg-red-50 text-red-800",
            ].join(" ")}
          >
            {success ? "Thanh cong" : "That bai"}
          </span>
        </div>
        <p className="mt-1 text-sm text-on-surface-variant">{formatDateTime(item.created_at)}</p>
        <p className="mt-1 text-sm text-on-surface-variant">
          IP: {item.ip_address || "—"}
        </p>
        {item.user_agent ? (
          <p className="mt-1 break-all text-xs text-on-surface-variant">{item.user_agent}</p>
        ) : null}
      </div>
    </li>
  );
}

export function LoginHistoryTab() {
  const { showSessionExpired } = useAuthSession();
  const [items, setItems] = useState([]);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [status, setStatus] = useState("loading");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchPage = useCallback(async (pageOffset, append = false) => {
    if (!append) {
      setStatus("loading");
    } else {
      setLoadMoreStatus("loading");
    }
    setErrorMessage("");

    try {
      const data = await getLoginHistory({ limit: PAGE_LIMIT, offset: pageOffset });
      const nextItems = data?.items || [];
      setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
      setOffset(pageOffset);
      setHasMore(nextItems.length === PAGE_LIMIT);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (!append) {
        setStatus("error");
      }
      setErrorMessage(error?.message || "Khong tai duoc lich su dang nhap.");
    } finally {
      setLoadMoreStatus("idle");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    fetchPage(0, false);
  }, [fetchPage]);

  const onLoadMore = () => {
    if (loadMoreStatus === "loading") return;
    fetchPage(offset + PAGE_LIMIT, true);
  };

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title="Lich su dang nhap"
          subtitle="Theo doi cac lan dang nhap thanh cong va that bai."
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title="Lich su dang nhap"
          subtitle="Theo doi cac lan dang nhap thanh cong va that bai."
        />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={() => fetchPage(0, false)}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thu lai
          </button>
        </AccountCard>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader
        title="Lich su dang nhap"
        subtitle="Theo doi cac lan dang nhap thanh cong va that bai."
      />

      {items.length === 0 ? (
        <EmptyState message="Chua co lich su dang nhap." />
      ) : (
        <AccountCard>
          <ul>
            {items.map((item) => (
              <HistoryRow key={item.id} item={item} />
            ))}
          </ul>

          {hasMore ? (
            <div className="mt-6 flex justify-center border-t border-outline-variant pt-6">
              <PrimaryButton
                type="button"
                onClick={onLoadMore}
                loading={loadMoreStatus === "loading"}
                className="!min-w-[160px]"
              >
                Tai them
              </PrimaryButton>
            </div>
          ) : null}
        </AccountCard>
      )}

      {errorMessage && status === "ready" ? (
        <p className="mt-4 text-sm text-error">{errorMessage}</p>
      ) : null}
    </div>
  );
}
