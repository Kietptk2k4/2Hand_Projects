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
        message: nextValue ? "Đã bat che do rieng tu." : "Đã tat che do rieng tu.",
      });
    } catch (error) {
      setIsPrivate(previous);
      setErrorMessage(error?.message || "Cập nhật that bai. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Quyen rieng tu"
        subtitle="Kiem soat kha nang hien thi hồ sơ của bạn voi cong dong."
      />

      <AccountCard>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="max-w-2xl">
            <h2 className="text-lg font-semibold text-on-surface">Chế độ hồ sơ rieng tu</h2>
            <p className="mt-2 text-sm text-on-surface-variant">
              {isPrivate
                ? "Chi hien thi ten và anh dai dien voi nguoi khac."
                : "Hồ sơ cong khai — moi nguoi xem duoc bio, website và lien ket."}
            </p>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={isPrivate}
            disabled={isSaving}
            onClick={onToggle}
            className={[
              "relative inline-flex h-7 w-12 shrink-0 items-center rounded-full transition",
              isPrivate ? "bg-primary" : "bg-outline-variant",
              isSaving ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            <span
              className={[
                "inline-block h-5 w-5 transform rounded-full bg-white shadow transition",
                isPrivate ? "translate-x-6" : "translate-x-1",
              ].join(" ")}
            />
            {isSaving ? (
              <span className="absolute -right-8 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin rounded-full border-2 border-primary/30 border-t-primary" />
            ) : null}
          </button>
        </div>

        <div className="mt-6 flex items-start gap-3 rounded-lg bg-account-surface-low p-4 text-sm text-on-surface-variant">
          <span aria-hidden="true">ℹ️</span>
          <p>
            Khi bat che do rieng tu, nguoi xem cong khai chi thay ten hien thi và anh dai dien. Bio, website va
            mang xa hoi sẽ bi an.
          </p>
        </div>

        {errorMessage ? <p className="mt-4 text-sm text-error">{errorMessage}</p> : null}
      </AccountCard>
    </div>
  );
}
