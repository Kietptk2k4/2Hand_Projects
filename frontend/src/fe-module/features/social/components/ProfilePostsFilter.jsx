import { PROFILE_STATUS_FILTERS } from "../constants/profilePostsConstants";

export function ProfilePostsFilter({ value, onChange, disabled = false }) {
  return (
    <div
      className="flex flex-wrap gap-2"
      role="group"
      aria-label="Lọc bài viết trên hồ sơ"
    >
      {PROFILE_STATUS_FILTERS.map((option) => {
        const isActive = value === option.value;
        return (
          <button
            key={option.value}
            type="button"
            disabled={disabled}
            aria-pressed={isActive}
            onClick={() => onChange?.(option.value)}
            className={[
              "rounded-lg border px-3 py-1.5 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50",
              isActive
                ? "border-primary bg-primary-fixed text-primary"
                : "border-outline-variant bg-surface-container-lowest text-on-surface-variant hover:border-primary hover:text-primary",
            ].join(" ")}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
