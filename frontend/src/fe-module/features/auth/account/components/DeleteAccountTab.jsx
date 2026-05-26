import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { softDeleteMyAccount } from "../../api/authApi";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import { AccountCard, PrimaryButton, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

export function DeleteAccountTab({ onNotify }) {
  const navigate = useNavigate();
  const { clearSession } = useAuthSession();
  const [password, setPassword] = useState("");
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [fieldError, setFieldError] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const openModal = () => {
    if (!password.trim()) {
      setFieldError("Vui long nhap mat khau hien tai.");
      return;
    }
    setFieldError("");
    setIsModalOpen(true);
  };

  const onConfirmDelete = async () => {
    setIsSubmitting(true);
    setFieldError("");
    try {
      await softDeleteMyAccount({ password });
      clearSession();
      onNotify?.({ variant: "success", message: "Tai khoan da duoc xoa." });
      navigate(APP_ROUTES.login, { replace: true });
    } catch (error) {
      const passwordError = error?.errors?.find((e) => e.field === "password");
      if (passwordError) {
        setFieldError(passwordError.reason || "Mat khau khong chinh xac.");
      } else {
        setFieldError(error?.message || "Co loi xay ra. Vui long thu lai.");
      }
      onNotify?.({ variant: "error", message: error?.message });
      setIsModalOpen(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader title="Xoa tai khoan" subtitle="Quan ly viec xoa tai khoan mot cach an toan." />

      <AccountCard className="overflow-hidden p-0">
        <div className="flex items-center gap-3 border-b border-error/20 bg-error-container px-6 py-4">
          <span className="text-xl" aria-hidden="true">
            ⚠️
          </span>
          <h2 className="text-lg font-semibold text-error">Vung nguy hiem</h2>
        </div>

        <div className="space-y-6 p-6">
          <p className="font-medium text-on-surface">Khi ban xoa tai khoan cua minh:</p>
          <ul className="space-y-4 border-l-2 border-outline-variant pl-4">
            <li>
              <p className="text-sm font-medium text-on-surface">Mat du lieu vinh vien</p>
              <p className="text-sm text-on-surface-variant">
                Tat ca thong tin ca nhan, cai dat va tuy chon se bi xoa khoi he thong.
              </p>
            </li>
            <li>
              <p className="text-sm font-medium text-on-surface">Xoa lich su</p>
              <p className="text-sm text-on-surface-variant">
                Lich su dat dich vu, danh gia va cac giao dich cu se khong the khoi phuc.
              </p>
            </li>
            <li>
              <p className="text-sm font-medium text-on-surface">Huy bo ket noi</p>
              <p className="text-sm text-on-surface-variant">
                Tat ca cac ket noi voi nha cung cap dich vu va khach hang se bi ngat.
              </p>
            </li>
          </ul>

          <div className="rounded-lg border border-outline-variant bg-account-surface-low p-4">
            <h3 className="mb-3 text-sm font-medium text-on-surface">Xac nhan mat khau de tiep tuc</h3>
            <div className="relative max-w-md">
              <input
                type={isPasswordVisible ? "text" : "password"}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setFieldError("");
                }}
                placeholder="Nhap mat khau hien tai"
                className={[
                  "w-full rounded-lg border bg-white px-3 py-2.5 pr-12 text-sm outline-none transition",
                  fieldError && !isModalOpen
                    ? "border-error focus:border-error"
                    : "border-outline-variant focus:border-primary",
                ].join(" ")}
              />
              <button
                type="button"
                onClick={() => setIsPasswordVisible((v) => !v)}
                className="absolute inset-y-0 right-0 px-3 text-sm text-on-surface-variant"
              >
                {isPasswordVisible ? "An" : "Hien"}
              </button>
            </div>
            {fieldError && !isModalOpen ? <p className="mt-2 text-sm text-error">{fieldError}</p> : null}
          </div>

          <div className="flex justify-end border-t border-outline-variant pt-6">
            <button
              type="button"
              onClick={openModal}
              className="rounded-lg bg-error px-6 py-2.5 text-sm font-medium text-white transition hover:opacity-90"
            >
              Xoa tai khoan
            </button>
          </div>
        </div>
      </AccountCard>

      {isModalOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-account-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isSubmitting) setIsModalOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-lg">
            <div className="flex items-start gap-4 p-6">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-error-container text-xl">
                🗑️
              </div>
              <div>
                <h3 id="delete-account-title" className="text-lg font-semibold text-on-surface">
                  Xac nhan xoa tai khoan?
                </h3>
                <p className="mt-2 text-sm text-on-surface-variant">
                  Ban sap xoa vinh vien quyen truy cap vao tai khoan nay. Hanh dong khong the hoan tac tu phia ban.
                </p>
                {fieldError && isModalOpen ? <p className="mt-2 text-sm text-error">{fieldError}</p> : null}
              </div>
            </div>
            <div className="flex justify-end gap-3 border-t border-outline-variant bg-account-surface-low px-6 py-4">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => setIsModalOpen(false)}
                className="rounded-lg px-4 py-2 text-sm font-medium text-on-surface-variant hover:text-on-surface"
              >
                Huy
              </button>
              <PrimaryButton
                type="button"
                onClick={onConfirmDelete}
                loading={isSubmitting}
                className="!bg-error hover:!opacity-90"
              >
                Xoa tai khoan
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
