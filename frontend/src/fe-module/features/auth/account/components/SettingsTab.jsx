import { useEffect, useMemo, useState } from "react";
import { updateMySettings } from "../../api/authApi";
import { useAppearance } from "../../context/AppearanceContext";
import { normalizeAppearanceMode } from "../../utils/appearanceTheme";
import { AccountCard, PrimaryButton, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

const THEME_OPTIONS = [
  { value: "LIGHT", label: "Sáng", icon: "☀️", previewClass: "bg-surface-container-lowest" },
  { value: "DARK", label: "Tối", icon: "🌙", previewClass: "bg-[#1a1c1e]" },
  {
    value: "SYSTEM",
    label: "Theo hệ thống",
    icon: "💻",
    hint: "Tự động theo cài đặt thiết bị của bạn",
    previewClass: "bg-gradient-to-br from-surface-container-lowest to-[#1a1c1e]",
  },
];

export function SettingsTab({ profile, refetch, onNotify }) {
  const { setAppearanceMode: applyAppearanceMode } = useAppearance();
  const savedModeFromDb = useMemo(
    () => normalizeAppearanceMode(profile?.settings?.appearance_mode),
    [profile?.settings?.appearance_mode]
  );
  const [appearanceMode, setAppearanceMode] = useState(savedModeFromDb);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setAppearanceMode(savedModeFromDb);
    setErrorMessage("");
  }, [savedModeFromDb]);

  const onSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");
    try {
      const data = await updateMySettings({ appearance_mode: appearanceMode });
      const savedMode = normalizeAppearanceMode(data?.appearance_mode || appearanceMode);
      applyAppearanceMode(savedMode);
      setAppearanceMode(savedMode);
      await refetch();
      onNotify?.({ variant: "success", message: "Cập nhật cài đặt thành công." });
    } catch (error) {
      setErrorMessage(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader title="Cài đặt" subtitle="Tùy chỉnh giao diện hiển thị của ứng dụng 2Hands." />

      <AccountCard>
        <form onSubmit={onSubmit}>
          <h2 className="mb-6 text-lg font-semibold text-on-surface">Giao diện</h2>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {THEME_OPTIONS.map((option) => {
              const selected = normalizeAppearanceMode(appearanceMode) === option.value;
              return (
                <label
                  key={option.value}
                  className={[
                    "cursor-pointer rounded-lg border-2 p-4 transition",
                    selected ? "border-primary bg-account-surface-low" : "border-outline-variant hover:border-primary/50",
                  ].join(" ")}
                >
                  <input
                    type="radio"
                    name="appearance_mode"
                    value={option.value}
                    checked={selected}
                    onChange={() => setAppearanceMode(option.value)}
                    className="sr-only"
                  />
                  <div
                    className={[
                      "mb-3 flex h-28 items-center justify-center rounded-lg border border-outline-variant text-4xl",
                      option.previewClass,
                    ].join(" ")}
                  >
                    {option.icon}
                  </div>
                  <div className="flex items-center gap-2">
                    <span
                      className={[
                        "flex h-4 w-4 items-center justify-center rounded-full border",
                        selected ? "border-primary" : "border-outline-variant",
                      ].join(" ")}
                    >
                      {selected ? <span className="h-2 w-2 rounded-full bg-primary" /> : null}
                    </span>
                    <span className="text-sm font-medium text-on-surface">{option.label}</span>
                  </div>
                  {option.hint ? <p className="mt-2 text-xs text-on-surface-variant">{option.hint}</p> : null}
                </label>
              );
            })}
          </div>

          {errorMessage ? <p className="mt-4 text-sm text-error">{errorMessage}</p> : null}

          <div className="mt-8 flex justify-end border-t border-outline-variant pt-6">
            <PrimaryButton type="submit" loading={isSubmitting}>
              Lưu cài đặt
            </PrimaryButton>
          </div>
        </form>
      </AccountCard>
    </div>
  );
}
