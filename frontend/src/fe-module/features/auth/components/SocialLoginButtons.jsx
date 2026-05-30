import { getOAuthRedirectUrl } from "../api/authApi";

const SOCIAL_BUTTONS = [
  { key: "google", label: "Tiếp tục với Google", icon: "G" },
  { key: "facebook", label: "Tiếp tục với Facebook", icon: "f" },
];

export function SocialLoginButtons({ disabled = false, onRedirectStart }) {
  const onSocialClick = (provider) => {
    const redirectUrl = getOAuthRedirectUrl(provider);
    if (!redirectUrl) return;
    onRedirectStart?.();
    window.location.href = redirectUrl;
  };

  return (
    <div className="space-y-3">
      {SOCIAL_BUTTONS.map((item) => (
        <button
          key={item.key}
          type="button"
          onClick={() => onSocialClick(item.key)}
          disabled={disabled}
          className="flex w-full items-center justify-center gap-2 rounded-lg border border-outline-variant bg-white px-4 py-3 text-sm font-medium text-on-surface transition hover:bg-surface disabled:cursor-not-allowed disabled:opacity-70"
        >
          <span className="inline-flex h-5 w-5 items-center justify-center rounded-full text-base font-semibold text-primary">
            {item.icon}
          </span>
          <span>{item.label}</span>
        </button>
      ))}
    </div>
  );
}
