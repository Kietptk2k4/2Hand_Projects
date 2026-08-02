import { useEffect, useState } from "react";
import { updateMyPrivacy } from "../../api/authApi";
import { AccountCard, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

export function PrivacyTab({ profile, refetch, onNotify }) {
  const isPrivateInitial = Boolean(profile?.profile?.is_private);
  const [isPrivate, setIsPrivate] = useState(isPrivateInitial);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setIsPrivate(isPrivateInitial);
    setErrorMessage("");
  }, [isPrivateInitial]);

  const onToggle = async () => {
    const nextValue = !isPrivate;
    const previous = isPrivate;
    setIsPrivate(nextValue);
    setIsSaving(true);
    setErrorMessage("");

    try {
      await updateMyPrivacy({ is_private: nextValue });
      await refetch();
      onNotify?.({
        variant: "success",
        message: nextValue ? "Đã bật chế độ riêng tư." : "Đã tắt chế độ riêng tư.",
      });
    } catch (error) {
      setIsPrivate(previous);
      setErrorMessage(error?.message || "Cập nhật thất bại. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Quyền riêng tư"
        subtitle="Kiểm soát khả năng hiển thị hồ sơ của bạn với cộng đồng."
      />

      <AccountCard>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="max-w-2xl">
            <h2 className="text-lg font-extrabold text-on-surface leading-tight">Chế độ hồ sơ riêng tư</h2>
            <p className="mt-1 text-xs text-on-surface-variant/70">
              {isPrivate
                ? "Chỉ hiển thị tên và ảnh đại diện với người khác."
                : "Hồ sơ công khai — mọi người xem được bio, website và liên kết."}
            </p>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={isPrivate}
            disabled={isSaving}
            onClick={onToggle}
            className={[
              "relative inline-flex h-7 w-12 shrink-0 items-center rounded-full transition-colors",
              isPrivate ? "bg-sky-500" : "bg-outline-variant/60",
              isSaving ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            <span
              className={[
                "inline-block h-5 w-5 transform rounded-full bg-white shadow-2xs transition-transform",
                isPrivate ? "translate-x-6" : "translate-x-1",
              ].join(" ")}
            />
            {isSaving ? (
              <span className="absolute -right-8 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin rounded-full border-2 border-sky-500/30 border-t-sky-500" />
            ) : null}
          </button>
        </div>

        <div className="mt-6 flex items-start gap-3 rounded-xl bg-surface-container-low/60 p-4 text-xs font-medium text-on-surface-variant/80">
          <span className="material-symbols-outlined text-[20px] text-sky-500 shrink-0" aria-hidden="true">
            info
          </span>
          <p>
            Khi bật chế độ riêng tư, người xem công khai chỉ thấy tên hiển thị và ảnh đại diện. Bio, website và
            mạng xã hội sẽ bị ẩn.
          </p>
        </div>

        {errorMessage ? <p className="mt-4 text-xs font-medium text-error">{errorMessage}</p> : null}
      </AccountCard>
    </div>
  );
}
