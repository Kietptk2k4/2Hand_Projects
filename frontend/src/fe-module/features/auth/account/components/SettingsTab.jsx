import { useEffect, useState } from "react";
import { updateMySettings } from "../../api/authApi";
import { AccountCard, PrimaryButton, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

const THEME_OPTIONS = [
  { value: "LIGHT", label: "Sang", icon: "☀️" },
  { value: "DARK", label: "Toi", icon: "🌙" },
  { value: "SYSTEM", label: "Theo he thong", icon: "💻", hint: "Tu dong theo cai dat thiet bi cua ban" },
];

export function SettingsTab({ profile, refetch, onNotify }) {
  const initialMode = profile?.settings?.appearance_mode || "SYSTEM";
  const [appearanceMode, setAppearanceMode] = useState(initialMode);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setAppearanceMode(initialMode);
    setErrorMessage("");
  }, [initialMode]);

  const onSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");
    try {
      await updateMySettings({ appearance_mode: appearanceMode });
      await refetch();
      onNotify?.({ variant: "success", message: "Cap nhat cai dat thanh cong." });
    } catch (error) {
      setErrorMessage(error?.message || "Co loi xay ra. Vui long thu lai.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader title="Cai dat" subtitle="Tuy chinh giao dien hien thi cua ung dung 2Hands." />

      <AccountCard>
        <form onSubmit={onSubmit}>
          <h2 className="mb-6 text-lg font-semibold text-on-surface">Giao dien</h2>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {THEME_OPTIONS.map((option) => {
              const selected = appearanceMode === option.value;
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
                  <div className="mb-3 flex h-28 items-center justify-center rounded-lg border border-outline-variant bg-white text-4xl">
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
              Luu cai dat
            </PrimaryButton>
          </div>
        </form>
      </AccountCard>
    </div>
  );
}
