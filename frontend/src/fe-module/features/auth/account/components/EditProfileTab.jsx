import { useEffect, useMemo, useState } from "react";
import { updateMyProfile } from "../../api/authApi";
import {
  mapSocialLinksToObject,
  mapSocialLinksToRows,
  SOCIAL_PLATFORMS,
  validateEditProfileForm,
} from "../accountSchemas.js";
import {
  AccountCard,
  AccountFieldLabel,
  AccountTextInput,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../shared/ui/auth/authUi.jsx";

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Truong du lieu khong hop le.";
    }
    return acc;
  }, {});
}

export function EditProfileTab({ profile, refetch, onNotify }) {
  const userProfile = profile?.profile || {};
  const [form, setForm] = useState({
    display_name: "",
    bio: "",
    website: "",
    social_links: [{ platform: "github", url: "" }],
  });
  const [errors, setErrors] = useState({});
  const [globalError, setGlobalError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setForm({
      display_name: userProfile.display_name || "",
      bio: userProfile.bio || "",
      website: userProfile.website || "",
      social_links: mapSocialLinksToRows(userProfile.social_links),
    });
    setErrors({});
    setGlobalError("");
  }, [userProfile.display_name, userProfile.bio, userProfile.website, userProfile.social_links]);

  const bioCount = form.bio?.length || 0;
  const nameCount = form.display_name?.length || 0;
  const validation = useMemo(() => validateEditProfileForm(form), [form]);

  const updateField = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setGlobalError("");
  };

  const updateSocialRow = (index, key, value) => {
    setForm((prev) => {
      const rows = [...prev.social_links];
      rows[index] = { ...rows[index], [key]: value };
      return { ...prev, social_links: rows };
    });
    setErrors((prev) => ({ ...prev, [`social_links.${index}.url`]: "" }));
  };

  const addSocialRow = () => {
    setForm((prev) => ({
      ...prev,
      social_links: [...prev.social_links, { platform: "other", url: "" }],
    }));
  };

  const removeSocialRow = (index) => {
    setForm((prev) => ({
      ...prev,
      social_links: prev.social_links.filter((_, i) => i !== index),
    }));
  };

  const resetForm = () => {
    setForm({
      display_name: userProfile.display_name || "",
      bio: userProfile.bio || "",
      website: userProfile.website || "",
      social_links: mapSocialLinksToRows(userProfile.social_links),
    });
    setErrors({});
    setGlobalError("");
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    const nextValidation = validateEditProfileForm(form);
    setErrors(nextValidation.errors);
    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    setGlobalError("");
    try {
      await updateMyProfile({
        display_name: form.display_name.trim(),
        bio: form.bio?.trim() || "",
        website: form.website?.trim() || "",
        social_links: mapSocialLinksToObject(form.social_links),
      });
      await refetch();
      onNotify?.({ variant: "success", message: "Cap nhat ho so thanh cong." });
    } catch (error) {
      const serverErrors = resolveFieldErrors(error?.errors);
      if (Object.keys(serverErrors).length > 0) {
        setErrors(serverErrors);
      }
      setGlobalError(error?.message || "Co loi xay ra. Vui long thu lai.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Chinh sua ho so"
        subtitle="Cap nhat thong tin ca nhan va cach ban xuat hien tren nen tang."
      />

      <AccountCard>
        <form onSubmit={onSubmit} className="flex flex-col gap-6" noValidate>
          {globalError ? <p className="text-sm text-error">{globalError}</p> : null}

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="display_name" required>
              Ten hien thi
            </AccountFieldLabel>
            <AccountTextInput
              id="display_name"
              name="display_name"
              value={form.display_name}
              onChange={updateField("display_name")}
              maxLength={100}
              error={errors.display_name}
            />
            <p className="text-xs text-on-surface-variant">{nameCount}/100</p>
          </div>

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="bio">Gioi thieu</AccountFieldLabel>
            <textarea
              id="bio"
              name="bio"
              rows={4}
              value={form.bio}
              onChange={updateField("bio")}
              maxLength={500}
              className={[
                "w-full resize-none rounded-lg border bg-white px-3 py-2.5 text-base outline-none transition",
                errors.bio
                  ? "border-error focus:border-error"
                  : "border-outline-variant focus:border-primary focus:ring-1 focus:ring-primary/30",
              ].join(" ")}
            />
            <p className="text-xs text-on-surface-variant">{bioCount}/500</p>
            {errors.bio ? <p className="text-sm text-error">{errors.bio}</p> : null}
          </div>

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="website">Website</AccountFieldLabel>
            <AccountTextInput
              id="website"
              name="website"
              value={form.website}
              onChange={updateField("website")}
              placeholder="https://example.com"
              error={errors.website}
            />
          </div>

          <hr className="border-outline-variant" />

          <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 className="text-base font-semibold text-on-surface">Lien ket mang xa hoi</h3>
                <p className="text-sm text-on-surface-variant">Them lien ket den cac trang ca nhan cua ban.</p>
              </div>
              <SecondaryButton type="button" onClick={addSocialRow} disabled={form.social_links.length >= 10}>
                + Them lien ket
              </SecondaryButton>
            </div>

            {form.social_links.map((row, index) => (
              <div
                key={`social-${index}`}
                className="flex flex-col gap-2 rounded-lg border border-outline-variant bg-account-surface-low p-3 sm:flex-row sm:items-center"
              >
                <select
                  value={row.platform}
                  onChange={(e) => updateSocialRow(index, "platform", e.target.value)}
                  className="rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm"
                >
                  {SOCIAL_PLATFORMS.map((p) => (
                    <option key={p.key} value={p.key}>
                      {p.label}
                    </option>
                  ))}
                </select>
                <input
                  type="url"
                  value={row.url}
                  onChange={(e) => updateSocialRow(index, "url", e.target.value)}
                  placeholder="https://"
                  className="min-w-0 flex-1 rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
                />
                <button
                  type="button"
                  onClick={() => removeSocialRow(index)}
                  className="text-sm text-on-surface-variant hover:text-error"
                  aria-label="Xoa lien ket"
                >
                  Xoa
                </button>
                {errors[`social_links.${index}.url`] ? (
                  <p className="w-full text-sm text-error sm:order-4">{errors[`social_links.${index}.url`]}</p>
                ) : null}
              </div>
            ))}
            {errors.social_links ? <p className="text-sm text-error">{errors.social_links}</p> : null}
          </div>

          <div className="flex justify-end gap-3 border-t border-outline-variant pt-6">
            <SecondaryButton type="button" onClick={resetForm} disabled={isSubmitting}>
              Huy
            </SecondaryButton>
            <PrimaryButton type="submit" loading={isSubmitting} disabled={!validation.isValid}>
              Luu thay doi
            </PrimaryButton>
          </div>
        </form>
      </AccountCard>
    </div>
  );
}
