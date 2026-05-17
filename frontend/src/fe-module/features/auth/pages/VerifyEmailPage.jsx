import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function VerifyEmailPage() {
  return (
    <section className="mx-auto w-full max-w-[520px] rounded-2xl border border-outline-variant/40 bg-white p-6 sm:p-8">
      <h1 className="text-2xl font-semibold text-on-surface">Kiem tra email cua ban</h1>
      <p className="mt-3 text-sm text-on-surface-variant">
        Tai khoan cua ban chua xac thuc email. Vui long kiem tra hop thu va lam theo huong dan de kich
        hoat tai khoan.
      </p>
      <div className="mt-6">
        <Link
          to={APP_ROUTES.login}
          className="inline-flex rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
        >
          Quay ve dang nhap
        </Link>
      </div>
    </section>
  );
}
