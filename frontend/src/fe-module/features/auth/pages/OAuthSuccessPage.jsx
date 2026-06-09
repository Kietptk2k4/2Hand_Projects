import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { fetchOAuthSession } from "../api/authApi";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "../hooks/useAuthSession.jsx";
import { USER_SESSION_KIND } from "../utils/adminSession";

function isSafeRedirectUrl(value) {
  return typeof value === "string" && value.startsWith("/") && !value.startsWith("//");
}

export function OAuthSuccessPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setSession } = useAuthSession();
  const [errorMessage, setErrorMessage] = useState("");

  const redirectUrl = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const value = params.get("redirectUrl");
    return isSafeRedirectUrl(value) ? value : APP_ROUTES.socialFeed;
  }, [location.search]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const status = params.get("status");

    if (status !== "success") {
      navigate(`${APP_ROUTES.oauthFailure}?status=error&code=AUTH-401-OAUTH-SESSION-INVALID`, {
        replace: true,
      });
      return;
    }

    let cancelled = false;

    (async () => {
      try {
        const sessionData = await fetchOAuthSession();
        if (cancelled) return;

        setSession({
          accessToken: sessionData.access_token,
          refreshToken: sessionData.refresh_token,
          user: sessionData.user,
          sessionKind: USER_SESSION_KIND,
        });

        const isPendingVerification = sessionData?.user?.status === "PENDING_VERIFICATION";
        navigate(isPendingVerification ? APP_ROUTES.verifyEmail : redirectUrl, {
          replace: true,
          state: isPendingVerification ? { email: sessionData?.user?.email } : undefined,
        });
      } catch (error) {
        if (cancelled) return;
        setErrorMessage(error?.message || GENERIC_ERROR_RETRY);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [location.search, navigate, redirectUrl, setSession]);

  if (errorMessage) {
    return (
      <section className="mx-auto flex min-h-[50vh] max-w-md flex-col items-center justify-center px-4 text-center">
        <p className="text-body-md text-error">{errorMessage}</p>
        <Link
          to={APP_ROUTES.login}
          className="mt-6 rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Về trang đăng nhập
        </Link>
      </section>
    );
  }

  return (
    <section className="mx-auto flex min-h-[50vh] max-w-md flex-col items-center justify-center px-4 text-center">
      <div
        className="mb-4 h-10 w-10 animate-spin rounded-full border-2 border-outline-variant border-t-primary"
        aria-hidden="true"
      />
      <p className="text-body-md text-on-surface-variant">Đang hoàn tất đăng nhập...</p>
    </section>
  );
}
