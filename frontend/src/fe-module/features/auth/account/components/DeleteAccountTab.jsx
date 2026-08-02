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
      setFieldError("Vui lòng nhập mật khẩu hiện tại.");
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
      onNotify?.({ variant: "success", message: "Tài khoản đã được xóa." });
      navigate(APP_ROUTES.login, { replace: true });
    } catch (error) {
      const passwordError = error?.errors?.find((e) => e.field === "password");
      if (passwordError) {
        setFieldError(passwordError.reason || "Mật khẩu không chính xác.");
      } else {
        setFieldError(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      }
      onNotify?.({ variant: "error", message: error?.message });
      setIsModalOpen(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader title="Xóa tài khoản" subtitle="Quản lý việc xóa tài khoản một cách an toàn." />

      <AccountCard className="overflow-hidden p-0">
        <div className="flex items-center gap-3 border-b border-error/20 bg-error-container/40 px-6 py-4">
          <span className="material-symbols-outlined text-2xl text-error" aria-hidden="true">
            warning
          </span>
          <h2 className="text-lg font-extrabold text-error">Vùng nguy hiểm</h2>
        </div>

        <div className="space-y-6 p-6">
          <p className="font-bold text-on-surface text-base">Khi bạn xóa tài khoản của mình:</p>
          <ul className="space-y-4 border-l-2 border-outline-variant/40 pl-4">
            <li>
              <p className="text-sm font-bold text-on-surface">Mất dữ liệu vĩnh viễn</p>
              <p className="text-xs text-on-surface-variant/70">
                Tất cả thông tin cá nhân, cài đặt và tùy chọn sẽ bị xóa khỏi hệ thống.
              </p>
            </li>
            <li>
              <p className="text-sm font-bold text-on-surface">Xóa lịch sử</p>
              <p className="text-xs text-on-surface-variant/70">
                Lịch sử đặt dịch vụ, đánh giá và các giao dịch cũ sẽ không thể khôi phục.
              </p>
            </li>
            <li>
              <p className="text-sm font-bold text-on-surface">Hủy bỏ kết nối</p>
              <p className="text-xs text-on-surface-variant/70">
                Tất cả các kết nối với nhà cung cấp dịch vụ và khách hàng sẽ bị ngắt.
              </p>
            </li>
          </ul>

          <div className="rounded-2xl border border-outline-variant/40 bg-surface-container-low/40 p-4">
            <h3 className="mb-3 text-xs font-bold text-on-surface">Xác nhận mật khẩu để tiếp tục</h3>
            <div className="relative max-w-md">
              <input
                type={isPasswordVisible ? "text" : "password"}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setFieldError("");
                }}
                placeholder="Nhập mật khẩu hiện tại"
                className={[
                  "w-full rounded-xl border bg-surface-container-lowest px-3.5 py-2.5 pr-12 text-sm text-on-surface outline-none transition placeholder:text-on-surface-variant/50",
                  fieldError && !isModalOpen
                    ? "border-error focus:border-error"
                    : "border-outline-variant/50 focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30",
                ].join(" ")}
              />
              <button
                type="button"
                onClick={() => setIsPasswordVisible((v) => !v)}
                className="absolute inset-y-0 right-0 px-3 text-xs font-bold text-on-surface-variant/70 hover:text-on-surface"
              >
                {isPasswordVisible ? "Ẩn" : "Hiện"}
              </button>
            </div>
            {fieldError && !isModalOpen ? <p className="mt-2 text-xs font-medium text-error">{fieldError}</p> : null}
          </div>

          <div className="flex justify-end border-t border-outline-variant/30 pt-6">
            <button
              type="button"
              onClick={openModal}
              className="rounded-full bg-red-600 px-6 py-2.5 text-sm font-bold text-white shadow-2xs transition-all hover:bg-red-700 active:scale-[0.97]"
            >
              Xóa tài khoản
            </button>
          </div>
        </div>
      </AccountCard>

      {isModalOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs"
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-account-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isSubmitting) setIsModalOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-2xl border border-outline-variant/40 bg-surface-container-lowest shadow-2xl">
            <div className="flex items-start gap-4 p-6">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-error-container/60 text-error">
                <span className="material-symbols-outlined text-2xl">delete_forever</span>
              </div>
              <div>
                <h3 id="delete-account-title" className="text-lg font-extrabold text-on-surface">
                  Xác nhận xóa tài khoản?
                </h3>
                <p className="mt-1 text-xs text-on-surface-variant/70">
                  Bạn sắp xóa vĩnh viễn quyền truy cập vào tài khoản này. Hành động không thể hoàn tác từ phía bạn.
                </p>
                {fieldError && isModalOpen ? <p className="mt-2 text-xs font-medium text-error">{fieldError}</p> : null}
              </div>
            </div>
            <div className="flex justify-end gap-3 border-t border-outline-variant/30 bg-surface-container-low/40 px-6 py-4">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => setIsModalOpen(false)}
                className="rounded-full border border-outline-variant/60 bg-surface-container-lowest px-5 py-2 text-xs font-bold text-on-surface hover:bg-surface-container-low active:scale-[0.97]"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={onConfirmDelete}
                disabled={isSubmitting}
                className="rounded-full bg-red-600 px-5 py-2 text-xs font-bold text-white shadow-2xs hover:bg-red-700 active:scale-[0.97] disabled:opacity-50"
              >
                {isSubmitting ? "Đang xóa..." : "Xóa tài khoản"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
