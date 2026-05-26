import { useCallback, useEffect, useState } from "react";
import { getAdminUserSessions } from "../../../api/authApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

const PAGE_LIMIT = 20;

const STATUS_OPTIONS = [
  { value: "ACTIVE", label: "Dang hoat dong" },
  { value: "LOGGED_OUT", label: "Da dang xuat" },
  { value: "REVOKED", label: "Da thu hoi" },
  { value: "EXPIRED", label: "Het han" },
  { value: "ALL", label: "Tat ca" },
];

const STATUS_BADGE_CLASS = {
  ACTIVE: "bg-green-50 text-green-800",
  LOGGED_OUT: "bg-gray-100 text-gray-800",
  REVOKED: "bg-red-50 text-red-800",
  EXPIRED: "bg-amber-50 text-amber-900",
};

function SessionCard({ session }) {
  const status = session.status || "ACTIVE";
  const badgeClass = STATUS_BADGE_CLASS[status] || "bg-gray-100 text-gray-800";

  return (
    <li className="rounded-lg border border-outline-variant bg-account-surface-low p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-medium text-on-surface">{session.device_id || "Thiet bi khong xac dinh"}</p>
          <p className="mt-1 break-all text-xs text-on-surface-variant">
            Session ID: {session.session_id || "—"}
          </p>
        </div>
        <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${badgeClass}`}>{status}</span>
      </div>
      <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-on-surface-variant">IP</dt>
          <dd className="text-on-surface">{session.ip_address || "—"}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant">Tao luc</dt>
          <dd className="text-on-surface">{formatDateTime(session.created_at)}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant">Cap nhat</dt>
          <dd className="text-on-surface">{formatDateTime(session.updated_at)}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-on-surface-variant">User agent</dt>
          <dd className="break-all text-on-surface">{session.user_agent || "—"}</dd>
        </div>
      </dl>
    </li>
  );
}

const EMPTY_USER_MESSAGE = "Vui long chon nguoi dung de xem du lieu.";

export function AdminUserSessionsTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [sessions, setSessions] = useState([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [status, setStatus] = useState("idle");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchPage = useCallback(
    async (targetPage, append = false, filterStatus = statusFilter) => {
      if (!userId) return;

      if (!append) {
        setStatus("loading");
      } else {
        setLoadMoreStatus("loading");
      }
      setErrorMessage("");

      try {
        const data = await getAdminUserSessions(userId, {
          page: targetPage,
          limit: PAGE_LIMIT,
          status: filterStatus,
        });
        const nextSessions = data?.sessions || [];
        const pagination = data?.pagination || {};

        setSessions((prev) => (append ? [...prev, ...nextSessions] : nextSessions));
        setPage(pagination.page ?? targetPage);
        setHasNext(pagination.has_next === true);
        setStatus("ready");
      } catch (error) {
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        if (error?.code === 403) {
          onNotify?.({ variant: "error", message: error?.message || "Ban khong co quyen truy cap." });
        }
        if (!append) {
          setStatus("error");
        }
        setErrorMessage(error?.message || "Khong tai duoc danh sach phien dang nhap.");
      } finally {
        setLoadMoreStatus("idle");
      }
    },
    [userId, statusFilter, showSessionExpired, onNotify]
  );

  useEffect(() => {
    if (!userId) {
      setSessions([]);
      setPage(1);
      setHasNext(false);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchPage(1, false, statusFilter);
  }, [userId, statusFilter, fetchPage]);

  const onLoadMore = () => {
    if (loadMoreStatus === "loading" || !hasNext) return;
    fetchPage(page + 1, true);
  };

  if (!userId) {
    return (
      <div>
        <TabPanelHeader
          title="Phien nguoi dung"
          subtitle="Xem cac phien dang nhap cua nguoi dung duoc chon."
        />
        <EmptyState message={EMPTY_USER_MESSAGE} />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title="Phien nguoi dung"
          subtitle="Xem cac phien dang nhap cua nguoi dung duoc chon."
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title="Phien nguoi dung"
          subtitle="Xem cac phien dang nhap cua nguoi dung duoc chon."
        />
        <AccountCard className="border-error/30 bg-error-container/30">
          <ErrorState message={errorMessage} />
          <p className="mt-2 text-sm text-on-surface-variant">
            He thong tam thoi khong phan hoi. Vui long thu lai sau vai phut.
          </p>
          <button
            type="button"
            onClick={() => fetchPage(1, false)}
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
        title="Phien nguoi dung"
        subtitle="Xem cac phien dang nhap cua nguoi dung duoc chon."
      />

      <AccountCard className="mb-6">
        <label htmlFor="session-status" className="mb-1 block text-xs font-semibold text-on-surface">
          Trang thai phien
        </label>
        <select
          id="session-status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="w-full max-w-xs rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </AccountCard>

      {sessions.length === 0 ? (
        <EmptyState message="Khong co phien dang nhap phu hop bo loc." />
      ) : (
        <AccountCard className="!p-0">
          <ul className="divide-y divide-outline-variant/50 p-2 sm:p-4">
            {sessions.map((session) => (
              <SessionCard key={session.session_id} session={session} />
            ))}
          </ul>

          {hasNext ? (
            <div className="flex justify-center border-t border-outline-variant px-4 py-6">
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
