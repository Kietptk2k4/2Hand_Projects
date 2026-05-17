import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function SessionExpiredPage() {
  return (
    <section className="mx-auto mt-10 w-full max-w-[520px] rounded-xl border border-outline-variant bg-white p-6 text-center shadow-sm">
      <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-surface-container">
        <span className="text-2xl text-primary" aria-hidden="true">
          ⏳
        </span>
      </div>
      <h1 className="text-3xl font-semibold text-on-surface">Session expired</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Phien dang nhap da het han, vui long dang nhap lai.
      </p>
      <Link
        to={APP_ROUTES.login}
        className="mt-6 inline-flex rounded bg-primary px-4 py-2 text-sm font-semibold text-white"
      >
        Sign In
      </Link>
    </section>
  );
}
