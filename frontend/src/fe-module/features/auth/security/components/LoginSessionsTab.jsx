import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getLoginSessions, logoutAllSessions } from "../../api/authApi";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../utils/formatDateTime.js";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import {
  AccountCard,
  AccountSkeleton,
  AuthAlert,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../shared/ui/PageState.jsx";

const LOGOUT_ALL_SUCCESS_MESSAGE = "Da dang xuat tat ca phien dang nhap.";

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
  const navigate = useNavigate();
  const { showSessionExpired, clearSession, hideSessionExpired } = useAuthSession();
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

  const onConfirmLogoutAll = async () => {
    setIsLoggingOutAll(true);
    setLogoutAllError("");

    try {
      await logoutAllSessions();
      hideSessionExpired();
      clearSession();
      navigate(APP_ROUTES.login, {
        replace: true,
        state: { logoutMessage: LOGOUT_ALL_SUCCESS_MESSAGE },
      });
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        setIsConfirmOpen(false);
        return;
      }
      if (error?.code === 500) {
        setLogoutAllError(error?.message || "Co loi xay ra. Vui long thu lai.");
        setIsConfirmOpen(false);
        return;
      }
      setLogoutAllError(error?.message || "Co loi xay ra. Vui long thu lai.");
      setIsConfirmOpen(false);
    } finally {
      setIsLoggingOutAll(false);
    }
  };

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

      {logoutAllError ? (
        <div className="mb-4">
          <AuthAlert variant="error" message={logoutAllError} />
          <button
            type="button"
            onClick={() => setLogoutAllError("")}
            className="mt-2 text-sm font-medium text-primary hover:underline"
          >
            Thu lai
          </button>
        </div>
      ) : null}

      <AccountCard className="mb-6">
        <p className="mb-3 text-sm text-on-surface-variant">
          Dang xuat khoi tat ca thiet bi dang dang nhap. Ban se can dang nhap lai.
        </p>
        <button
          type="button"
          onClick={() => setIsConfirmOpen(true)}
          disabled={isLoggingOutAll}
          className="rounded-lg border border-error px-4 py-2.5 text-sm font-semibold text-error transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
        >
          Dang xuat tat ca thiet bi
        </button>
      </AccountCard>

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

      {isConfirmOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="logout-all-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isLoggingOutAll) setIsConfirmOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-lg">
            <div className="p-6">
              <h3 id="logout-all-title" className="text-lg font-semibold text-on-surface">
                Dang xuat tat ca thiet bi?
              </h3>
              <p className="mt-2 text-sm text-on-surface-variant">
                Ban se can dang nhap lai tren cac thiet bi khac.
              </p>
            </div>
            <div className="flex justify-end gap-3 border-t border-outline-variant bg-account-surface-low px-6 py-4">
              <SecondaryButton
                type="button"
                disabled={isLoggingOutAll}
                onClick={() => setIsConfirmOpen(false)}
              >
                Huy
              </SecondaryButton>
              <PrimaryButton type="button" loading={isLoggingOutAll} onClick={onConfirmLogoutAll}>
                Dang xuat
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
