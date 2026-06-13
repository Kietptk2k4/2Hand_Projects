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

  return (
    <section className="relative w-full">
      <button
        type="button"
        onClick={() => setImagePreview("cover")}
        className="relative block h-48 w-full cursor-zoom-in overflow-hidden bg-surface-container-highest md:h-64"
        aria-label="Xem ảnh bìa"
        style={{
          backgroundImage: `url('${resolvedCoverUrl}')`,
          backgroundSize: "cover",
          backgroundPosition: "center",
        }}
      >
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-on-background/20 to-transparent" />
      </button>

      <div className="relative z-10 flex flex-col items-center px-4 md:px-8 -mt-16 md:-mt-20">
        <button
          type="button"
          onClick={() => setImagePreview("avatar")}
          className="h-32 w-32 cursor-zoom-in overflow-hidden rounded-full border-4 border-surface bg-surface-container shadow-sm transition-opacity hover:opacity-95 md:h-40 md:w-40"
          aria-label="Xem ảnh đại diện"
        >
          <img
            src={avatarUrl}
            alt=""
            className="h-full w-full object-cover"
          />
        </button>

        <div className="mt-4 flex items-center gap-1">
          <h1 className="text-center text-2xl font-semibold text-on-surface md:text-3xl">
            {profile.displayName}
          </h1>
          {isPrivateAccount ? (
            <span
              className="material-symbols-outlined text-on-surface-variant"
              title="Tài khoản riêng tư"
              aria-hidden="true"
            >
              lock
            </span>
          ) : (
            <span
              className="material-symbols-outlined text-primary"
              style={{ fontVariationSettings: "'FILL' 1" }}
              title="Đã xác minh"
              aria-hidden="true"
            >
              verified
            </span>
          )}
        </div>

        {showPrivateNotice ? (
          <p className="mt-2 max-w-2xl text-center text-sm text-on-surface-variant">
            Tài khoản đang ở chế độ riêng tư.
          </p>
        ) : null}

        {isDetailsLoading ? (
          <div
            className="mt-3 h-4 w-48 animate-pulse rounded bg-surface-container-highest"
            aria-label="Đang tải thông tin hồ sơ"
          />
        ) : null}

        {detailsError ? (
          <div className="mt-2 text-center">
            <p className="text-sm text-on-surface-variant">{detailsError}</p>
            {onDetailsRetry ? (
              <button
                type="button"
                onClick={onDetailsRetry}
                className="mt-1 text-sm font-medium text-primary hover:underline"
              >
                Thử lại
              </button>
            ) : null}
          </div>
        ) : null}

        {showDetails && bio ? (
          <p className="mt-2 max-w-2xl text-center text-sm text-on-surface-variant md:text-base">
            {bio}
          </p>
        ) : null}

        {showDetails && hasWebsite ? (
          <a
            href={website}
            target="_blank"
            rel="noreferrer"
            className="mt-2 text-sm text-primary hover:underline"
          >
            {website}
          </a>
        ) : null}

        {showDetails && socialLinkEntries.length > 0 ? (
          <ul className="mt-2 flex flex-wrap justify-center gap-x-4 gap-y-1">
            {socialLinkEntries.map(([key, url]) => (
              <li key={key}>
                <a
                  href={url}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm text-primary hover:underline"
                >
                  {key}
                </a>
              </li>
            ))}
          </ul>
        ) : null}

        {showCounters ? (
          <div className="mt-4 flex gap-8">
            <button
              type="button"
              onClick={onFollowersClick}
              className="flex flex-col items-center hover:text-primary"
            >
              <span className="text-lg font-semibold text-on-surface">{followerDisplay}</span>
              <span className="text-sm text-on-surface-variant">Người theo dõi</span>
            </button>
            <button
              type="button"
              onClick={onFollowingClick}
              className="flex flex-col items-center hover:text-primary"
            >
              <span className="text-lg font-semibold text-on-surface">{followingDisplay}</span>
              <span className="text-sm text-on-surface-variant">Đang theo dõi</span>
            </button>
          </div>
        ) : null}

        <div className="mt-4 flex flex-wrap justify-center gap-3">
          {commerceShop?.hasShop && commerceShop.shopId ? (
            <Link
              to={buildCommerceShopPath(commerceShop.shopId)}
              className="inline-flex items-center gap-2 rounded-lg border-2 border-outline-variant px-5 py-2.5 text-sm font-medium text-on-surface transition-colors hover:border-primary hover:text-primary"
            >
              <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                storefront
              </span>
              {commerceShop.shopName ? `Shop: ${commerceShop.shopName}` : "Xem shop"}
            </Link>
          ) : null}
          {isSelf ? (
            <>
              <Link
                to={APP_ROUTES.account}
                className="rounded-lg bg-primary px-5 py-2.5 text-sm font-medium text-on-primary shadow-sm transition-colors hover:bg-[#0050cb]"
              >
                Chỉnh sửa hồ sơ
              </Link>
              <Link
                to={APP_ROUTES.account}
                className="rounded-lg border-2 border-outline-variant px-5 py-2.5 text-sm font-medium text-on-surface transition-colors hover:border-primary hover:text-primary"
              >
                Cài đặt tài khoản
              </Link>
            </>
          ) : showFollowButton ? (
            <button
              type="button"
              onClick={onFollowClick}
              disabled={followDisabled || isFollowLoading}
              title={followDisabled ? followDisabledTitle : undefined}
              className={[
                "inline-flex min-w-[140px] items-center justify-center gap-2 rounded-lg px-5 py-2.5 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60",
                profile.followStatus === "NONE"
                  ? "bg-primary text-on-primary shadow-sm hover:bg-[#0050cb]"
                  : "border-2 border-outline-variant text-on-surface hover:border-primary hover:text-primary",
              ].join(" ")}
            >
              {isFollowLoading ? (
                <span
                  className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
                  aria-hidden="true"
                />
              ) : null}
              {followLabel}
            </button>
          ) : null}
        </div>
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
