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
      acc[item.field] = item.reason || "Trường dữ liệu không hợp lệ.";
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
      onNotify?.({ variant: "success", message: "Cập nhật hồ sơ thành công." });
    } catch (error) {
      const serverErrors = resolveFieldErrors(error?.errors);
      if (Object.keys(serverErrors).length > 0) {
        setErrors(serverErrors);
      }
      setGlobalError(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Chỉnh sửa hồ sơ"
        subtitle="Cập nhật thông tin cá nhân và cách bạn xuất hiện trên nền tảng."
      />

      <AccountCard>
        <form onSubmit={onSubmit} className="flex flex-col gap-6" noValidate>
          {globalError ? <p className="text-sm text-error">{globalError}</p> : null}

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="display_name" required>
              Tên hiển thị
            </AccountFieldLabel>
            <AccountTextInput
              id="display_name"
              name="display_name"
              value={form.display_name ?? ""}
              onChange={updateField("display_name")}
              maxLength={100}
              error={errors.display_name}
            />
            <p className="text-xs text-on-surface-variant">{nameCount}/100</p>
          </div>

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="bio">Giới thiệu</AccountFieldLabel>
            <textarea
              id="bio"
              name="bio"
              rows={4}
              value={form.bio ?? ""}
              onChange={updateField("bio")}
              maxLength={500}
              placeholder="Viết vài dòng giới thiệu về bản thân bạn..."
              className={[
                "w-full resize-none rounded-xl border bg-surface-container-lowest px-3.5 py-2.5 text-sm text-on-surface outline-none transition placeholder:text-on-surface-variant/50",
                errors.bio
                  ? "border-error focus:border-error focus:ring-1 focus:ring-error"
                  : "border-outline-variant/50 focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30",
              ].join(" ")}
            />
            <p className="text-xs text-on-surface-variant/70">{bioCount}/500</p>
            {errors.bio ? <p className="text-xs text-error font-medium">{errors.bio}</p> : null}
          </div>

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="website">Website</AccountFieldLabel>
            <AccountTextInput
              id="website"
              name="website"
              value={form.website ?? ""}
              onChange={updateField("website")}
              placeholder="https://example.com"
              error={errors.website}
            />
          </div>

          <hr className="border-outline-variant/30" />

          <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 className="text-base font-extrabold text-on-surface">Liên kết mạng xã hội</h3>
                <p className="text-xs text-on-surface-variant/70">Thêm liên kết đến các trang cá nhân của bạn.</p>
              </div>
              <SecondaryButton type="button" onClick={addSocialRow} disabled={form.social_links.length >= 10}>
                + Thêm liên kết
              </SecondaryButton>
            </div>

            {form.social_links.map((row, index) => (
              <div
                key={`social-${index}`}
                className="flex flex-col gap-2 rounded-xl border border-outline-variant/40 bg-surface-container-low/40 p-3 sm:flex-row sm:items-center"
              >
                <select
                  value={row.platform}
                  onChange={(e) => updateSocialRow(index, "platform", e.target.value)}
                  className="rounded-xl border border-outline-variant/50 bg-surface-container-lowest px-3 py-2 text-xs font-bold text-on-surface outline-none focus:border-sky-500"
                >
                  {SOCIAL_PLATFORMS.map((p) => (
                    <option key={p.key} value={p.key}>
                      {p.label}
                    </option>
                  ))}
                </select>
                <input
                  type="url"
                  value={row.url ?? ""}
                  onChange={(e) => updateSocialRow(index, "url", e.target.value)}
                  placeholder="https://"
                  className="min-w-0 flex-1 rounded-xl border border-outline-variant/50 bg-surface-container-lowest px-3.5 py-2 text-sm text-on-surface outline-none transition focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30"
                />
                <button
                  type="button"
                  onClick={() => removeSocialRow(index)}
                  className="text-xs font-bold text-on-surface-variant/70 hover:text-error transition-colors px-2"
                  aria-label="Xóa liên kết"
                >
                  Xóa
                </button>
                {errors[`social_links.${index}.url`] ? (
                  <p className="w-full text-xs text-error font-medium sm:order-4">{errors[`social_links.${index}.url`]}</p>
                ) : null}
              </div>
            ))}
            {errors.social_links ? <p className="text-xs text-error font-medium">{errors.social_links}</p> : null}
          </div>

          <div className="flex justify-end gap-3 border-t border-outline-variant/30 pt-6">
            <SecondaryButton type="button" onClick={resetForm} disabled={isSubmitting}>
              Hủy
            </SecondaryButton>
            <PrimaryButton type="submit" loading={isSubmitting} disabled={!validation.isValid}>
              Lưu thay đổi
            </PrimaryButton>
          </div>
        </form>
      </AccountCard>
    </div>
  );
}
