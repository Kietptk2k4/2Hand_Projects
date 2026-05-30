import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { COVER_IMAGE_URL } from "../constants/socialProfileConstants";

const DEFAULT_AVATAR = "https://i.pravatar.cc/200?img=11";

function formatCount(value) {
  if (value === null || value === undefined) return null;
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}

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
  bio,
  onFollowClick,
  onFollowersClick,
  onFollowingClick,
  isFollowLoading = false,
  followDisabled = false,
  followDisabledTitle,
}) {
  if (!profile) return null;

  const isSelf = profile.followStatus === "SELF";
  const followLabel = followButtonLabel(profile.followStatus);
  const followerDisplay = formatCount(profile.followerCount);
  const followingDisplay = formatCount(profile.followingCount);
  const showCounters = followerDisplay !== null && followingDisplay !== null;

  return (
    <section className="relative w-full">
      <div
        className="relative h-48 w-full overflow-hidden bg-surface-container-highest md:h-64"
        style={{
          backgroundImage: `url('${COVER_IMAGE_URL}')`,
          backgroundSize: "cover",
          backgroundPosition: "center",
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-t from-on-background/20 to-transparent" />
      </div>

      <div className="relative z-10 flex flex-col items-center px-4 md:px-8 -mt-16 md:-mt-20">
        <div className="h-32 w-32 overflow-hidden rounded-full border-4 border-surface bg-surface-container shadow-sm md:h-40 md:w-40">
          <img
            src={profile.avatarUrl || DEFAULT_AVATAR}
            alt=""
            className="h-full w-full object-cover"
          />
        </div>

        <div className="mt-4 flex items-center gap-1">
          <h1 className="text-center text-2xl font-semibold text-on-surface md:text-3xl">
            {profile.displayName}
          </h1>
          {profile.isPrivate ? (
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
              title="Verified"
              aria-hidden="true"
            >
              verified
            </span>
          )}
        </div>

        {bio ? (
          <p className="mt-2 max-w-2xl text-center text-sm text-on-surface-variant md:text-base">
            {bio}
          </p>
        ) : null}

        {showCounters ? (
          <div className="mt-4 flex gap-8">
            <button
              type="button"
              onClick={onFollowersClick}
              className="flex flex-col items-center hover:text-primary"
            >
              <span className="text-lg font-semibold text-on-surface">{followerDisplay}</span>
              <span className="text-sm text-on-surface-variant">Followers</span>
            </button>
            <button
              type="button"
              onClick={onFollowingClick}
              className="flex flex-col items-center hover:text-primary"
            >
              <span className="text-lg font-semibold text-on-surface">{followingDisplay}</span>
              <span className="text-sm text-on-surface-variant">Following</span>
            </button>
          </div>
        ) : null}

        <div className="mt-4 flex flex-wrap justify-center gap-3">
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
          ) : followLabel ? (
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
    </section>
  );
}
