import { useCallback, useEffect, useState } from "react";
import { getLoginSessions } from "../../api/authApi";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../utils/formatDateTime.js";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../shared/ui/PageState.jsx";

function SessionRow({ session }) {
  return (
    <li className="rounded-lg border border-outline-variant bg-account-surface-low p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <p className="font-medium text-on-surface">{session.device_id || "Thiet bi khong xac dinh"}</p>
        <span className="rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-semibold text-green-800">
          {session.status || "ACTIVE"}
        </span>
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
        <div className="sm:col-span-2">
          <dt className="text-on-surface-variant">User agent</dt>
          <dd className="break-all text-on-surface">{session.user_agent || "—"}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant">Het han</dt>
          <dd className="text-on-surface">{formatDateTime(session.expires_at)}</dd>
        </div>
      </dl>
    </li>
  );
}

export function LoginSessionsTab() {
  const { showSessionExpired } = useAuthSession();
  const [sessions, setSessions] = useState([]);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await getLoginSessions();
      setSessions(data?.sessions || []);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc danh sach phien dang nhap.");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title="Phien dang nhap"
          subtitle="Xem cac thiet bi dang dang nhap vao tai khoan cua ban."
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title="Phien dang nhap"
          subtitle="Xem cac thiet bi dang dang nhap vao tai khoan cua ban."
        />
        <AccountCard className="border-error/30 bg-error-container/30">
          <ErrorState message={errorMessage} />
          <p className="mt-2 text-sm text-on-surface-variant">
            He thong tam thoi khong phan hoi. Vui long thu lai sau vai phut.
          </p>
          <button
            type="button"
            onClick={load}
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
        title="Phien dang nhap"
        subtitle="Xem cac thiet bi dang dang nhap vao tai khoan cua ban."
      />

      {sessions.length === 0 ? (
        <EmptyState message="Khong co phien dang nhap dang hoat dong." />
      ) : (
        <AccountCard className="!p-0">
          <ul className="divide-y divide-outline-variant/50 p-2 sm:p-4">
            {sessions.map((session) => (
              <SessionRow key={session.id} session={session} />
            ))}
          </ul>
        </AccountCard>
      )}
    </div>
  );
}
