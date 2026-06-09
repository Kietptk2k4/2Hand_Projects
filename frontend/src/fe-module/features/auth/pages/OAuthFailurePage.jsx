import { useMemo } from "react";
import { Link, useLocation } from "react-router-dom";
import { mapOAuthFailureMessage } from "../constants/oauthConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function OAuthFailurePage() {
  const location = useLocation();

  const message = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const code = params.get("code");
    return mapOAuthFailureMessage(code);
  }, [location.search]);

  return (
    <section className="mx-auto flex min-h-[50vh] max-w-md flex-col items-center justify-center px-4 text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-error-container text-error">
        <span className="material-symbols-outlined text-3xl" aria-hidden="true">
          error
        </span>
      </div>
      <h1 className="text-headline-sm font-semibold text-on-surface">Đăng nhập thất bại</h1>
      <p className="mt-3 text-body-md text-on-surface-variant">{message}</p>
      <Link
        to={APP_ROUTES.login}
        className="mt-8 rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Thử đăng nhập lại
      </Link>
    </section>
  );
}
