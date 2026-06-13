const URL_PATTERN = /^https?:\/\/.+/i;
const MAX_DISPLAY_NAME = 100;
const MAX_BIO = 500;
const MAX_SOCIAL_LINKS = 10;

export const SOCIAL_PLATFORMS = [
  { key: "facebook", label: "Facebook" },
  { key: "github", label: "GitHub" },
  { key: "instagram", label: "Instagram" },
  { key: "tiktok", label: "TikTok" },
  { key: "other", label: "Khac" },
];

export function isValidHttpUrl(value) {
  if (!value || !value.trim()) return true;
  return URL_PATTERN.test(value.trim());
}

export function validateEditProfileForm(form) {
  const errors = {};
  const displayName = (form.display_name || "").trim();

  if (!displayName) {
    errors.display_name = "Ten hien thi la bat buoc.";
  } else if (displayName.length > MAX_DISPLAY_NAME) {
    errors.display_name = `Ten hien thi toi da ${MAX_DISPLAY_NAME} ký tự.`;
  }

  if ((form.bio || "").length > MAX_BIO) {
    errors.bio = `Giới thiệu toi da ${MAX_BIO} ký tự.`;
  }

  if (!isValidHttpUrl(form.website)) {
    errors.website = "URL không hợp lệ (can bat dau bang http hoac https).";
  }

  const socialRows = form.social_links || [];
  if (socialRows.length > MAX_SOCIAL_LINKS) {
    errors.social_links = `Toi da ${MAX_SOCIAL_LINKS} lien ket.`;
  }

  socialRows.forEach((row, index) => {
    if (row.url && !isValidHttpUrl(row.url)) {
      errors[`social_links.${index}.url`] = "URL không hợp lệ.";
    }
  });

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export function mapSocialLinksToObject(rows) {
  return rows.reduce((acc, row) => {
    const key = (row.platform || "").trim();
    const url = (row.url || "").trim();
    if (key && url) acc[key] = url;
    return acc;
  }, {});
}

export function mapSocialLinksToRows(socialLinks = {}) {
  const entries = Object.entries(socialLinks || {});
  if (entries.length === 0) {
    return [{ platform: "github", url: "" }];
  }
  return entries.map(([platform, url]) => ({ platform, url: url ?? "" }));
}

export const AVATAR_MAX_BYTES = 5 * 1024 * 1024;
export const AVATAR_ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];
export const COVER_MAX_BYTES = AVATAR_MAX_BYTES;
export const COVER_ALLOWED_TYPES = AVATAR_ALLOWED_TYPES;
