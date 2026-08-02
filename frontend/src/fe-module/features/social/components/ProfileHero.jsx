import { useState } from "react";
import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildCommerceShopPath } from "../../commerce/utils/commerceRoutes";
import { COVER_IMAGE_URL } from "../constants/socialProfileConstants";
import { formatSocialCount } from "../utils/formatSocialCount";
import { ProfileImageLightbox } from "./ProfileImageLightbox";

const DEFAULT_AVATAR = "https://i.pravatar.cc/200?img=11";

function followButtonLabel(followStatus) {
  switch (followStatus) {
    case "SELF":
      return null;
    case "PENDING":
      return "Đã gửi yêu cầu";
    case "ACCEPTED":
      return "Đang theo dõi";
    default:
      return "Theo dõi";
  }
}

export function ProfileHero({
  profile,
  coverImageUrl,
  bio,
  website,
  socialLinks = {},
  showPrivateNotice = false,
  isDetailsLoading = false,
  detailsError = "",
  onDetailsRetry,
  onFollowClick,
  onFollowersClick,
  onFollowingClick,
  isFollowLoading = false,
  followDisabled = false,
  followDisabledTitle,
  commerceShop,
}) {
  const [imagePreview, setImagePreview] = useState(null);

  if (!profile) return null;

  const isSelf = profile.followStatus === "SELF";
  const isPrivateAccount = Boolean(profile.is_private ?? profile.isPrivate);
  const followLabel = followButtonLabel(profile.followStatus);
  const showFollowButton = Boolean(followLabel) && !isPrivateAccount;
  const followerDisplay = formatSocialCount(profile.followerCount);
  const followingDisplay = formatSocialCount(profile.followingCount);
  const showCounters = followerDisplay !== null && followingDisplay !== null;
  const socialLinkEntries = Object.entries(socialLinks || {}).filter(
    ([, url]) => String(url || "").trim()
  );
  const hasWebsite = Boolean(String(website || "").trim());
  const showDetails = !showPrivateNotice && !isDetailsLoading && !detailsError;
  const resolvedCoverUrl =
    String(coverImageUrl || profile.coverUrl || profile.cover_url || "").trim() || COVER_IMAGE_URL;
  const avatarUrl = profile.avatarUrl || DEFAULT_AVATAR;

  const userEmail = profile.email || profile.username || profile.handle || "";
  const userHandle = userEmail
    ? (userEmail.includes("@") ? `@${userEmail.split("@")[0]}` : `@${userEmail}`)
    : `@${(profile.displayName || "user").toLowerCase().replace(/[^a-z0-9]/gi, "")}`;

  return (
    <section className="relative w-full border-b border-outline-variant/40">
      {/* Cover Image Banner */}
      <button
        type="button"
        onClick={() => setImagePreview("cover")}
        className="group relative block h-44 w-full cursor-zoom-in overflow-hidden bg-surface-container-high md:h-52"
        aria-label="Xem ảnh bìa"
      >
        <img
          src={resolvedCoverUrl}
          alt=""
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.02]"
        />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent" />
      </button>

      {/* Profile Header Row (Avatar + Action Buttons) */}
      <div className="relative px-4 pb-4">
        <div className="flex items-end justify-between -mt-14 md:-mt-16 mb-3">
          <button
            type="button"
            onClick={() => setImagePreview("avatar")}
            className="group relative h-28 w-28 cursor-zoom-in overflow-hidden rounded-full border-4 border-surface-container-lowest bg-surface-container-high shadow-md transition-transform duration-200 hover:scale-105 md:h-32 md:w-32 shrink-0"
            aria-label="Xem ảnh đại diện"
          >
            <img
              src={avatarUrl}
              alt=""
              className="h-full w-full object-cover"
            />
          </button>

          {/* Right Action Buttons */}
          <div className="flex flex-wrap items-center gap-2 pt-2">
            {commerceShop?.hasShop && commerceShop.shopId ? (
              <Link
                to={buildCommerceShopPath(commerceShop.shopId)}
                className="inline-flex items-center gap-1.5 rounded-full border border-outline-variant/60 bg-surface-container-lowest px-4 py-1.5 text-sm font-bold text-on-surface shadow-2xs transition-all hover:bg-surface-container-low active:scale-95"
              >
                <span className="material-symbols-outlined text-[18px] text-sky-500" aria-hidden="true">
                  storefront
                </span>
                <span>{commerceShop.shopName ? `Shop: ${commerceShop.shopName}` : "Xem Shop"}</span>
              </Link>
            ) : null}

            {isSelf ? (
              <Link
                to={APP_ROUTES.account}
                className="inline-flex items-center rounded-full border border-outline-variant/60 bg-surface-container-lowest px-4 py-1.5 text-sm font-bold text-on-surface shadow-2xs transition-all hover:bg-surface-container-low active:scale-95"
              >
                Chỉnh sửa hồ sơ
              </Link>
            ) : showFollowButton ? (
              <button
                type="button"
                onClick={onFollowClick}
                disabled={followDisabled || isFollowLoading}
                title={followDisabled ? followDisabledTitle : undefined}
                className={[
                  "inline-flex items-center justify-center rounded-full px-5 py-1.5 text-sm font-bold transition-all shadow-2xs active:scale-95",
                  profile.followStatus === "NONE"
                    ? "bg-on-surface text-surface-container-lowest hover:opacity-90"
                    : "border border-outline-variant/60 text-on-surface hover:bg-surface-container-high",
                  followDisabled || isFollowLoading ? "cursor-not-allowed opacity-60" : "",
                ].join(" ")}
              >
                {isFollowLoading ? (
                  <span
                    className="mr-2 inline-block h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent"
                    aria-hidden="true"
                  />
                ) : null}
                {followLabel}
              </button>
            ) : null}
          </div>
        </div>

        {/* User Display Name & Handle */}
        <div className="mt-1">
          <div className="flex items-center gap-1">
            <h1 className="text-xl font-extrabold text-on-surface md:text-2xl leading-tight">
              {profile.displayName}
            </h1>
            {isPrivateAccount ? (
              <span
                className="material-symbols-outlined text-[20px] text-on-surface-variant/70"
                title="Tài khoản riêng tư"
                aria-hidden="true"
              >
                lock
              </span>
            ) : (
              <span
                className="material-symbols-outlined text-[20px] text-sky-500"
                style={{ fontVariationSettings: "'FILL' 1" }}
                title="Đã xác minh"
                aria-hidden="true"
              >
                verified
              </span>
            )}
          </div>
          <p className="text-[15px] text-on-surface-variant/70">{userHandle}</p>
        </div>

        {/* Bio */}
        {showDetails && bio ? (
          <p className="mt-2.5 text-[15px] leading-relaxed text-on-surface">
            {bio}
          </p>
        ) : null}

        {/* Meta Info Row: Location, Website, Joined Date */}
        {showDetails ? (
          <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[15px] text-on-surface-variant/70">
            {hasWebsite ? (
              <a
                href={website}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 text-sky-500 hover:underline"
              >
                <span className="material-symbols-outlined text-[18px]">link</span>
                <span className="truncate max-w-[200px]">{website.replace(/^https?:\/\//, "")}</span>
              </a>
            ) : null}

            {socialLinkEntries.map(([key, url]) => (
              <a
                key={key}
                href={url}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 text-sky-500 hover:underline"
              >
                <span className="material-symbols-outlined text-[18px]">public</span>
                <span>{key}</span>
              </a>
            ))}

            <div className="flex items-center gap-1">
              <span className="material-symbols-outlined text-[18px]">calendar_month</span>
              <span>Đã tham gia tháng 7, 2026</span>
            </div>
          </div>
        ) : null}

        {/* Following & Followers Counts */}
        {showCounters ? (
          <div className="mt-3 flex items-center gap-5 text-[15px]">
            <button
              type="button"
              onClick={onFollowingClick}
              className="flex items-center gap-1 text-on-surface-variant hover:underline"
            >
              <span className="font-bold text-on-surface">{followingDisplay}</span>
              <span>Đang theo dõi</span>
            </button>
            <button
              type="button"
              onClick={onFollowersClick}
              className="flex items-center gap-1 text-on-surface-variant hover:underline"
            >
              <span className="font-bold text-on-surface">{followerDisplay}</span>
              <span>Người theo dõi</span>
            </button>
          </div>
        ) : null}
      </div>

      {imagePreview === "avatar" ? (
        <ProfileImageLightbox
          imageUrl={avatarUrl}
          label="Ảnh đại diện"
          onClose={() => setImagePreview(null)}
        />
      ) : null}

      {imagePreview === "cover" ? (
        <ProfileImageLightbox
          imageUrl={resolvedCoverUrl}
          label="Ảnh bìa"
          onClose={() => setImagePreview(null)}
        />
      ) : null}
    </section>
  );
}
