import { useNavigate } from "react-router-dom";
import { MOCK_SOCIAL_USER_IDS } from "../constants/socialProfileConstants";
import { SUGGESTED_PROVIDERS } from "../constants/suggestedProviders";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const SEARCH_USERS = [
  {
    userId: MOCK_SOCIAL_USER_IDS.ACTIVE,
    name: "Active User (bạn)",
    role: "Tài khoản đang đăng nhập",
    avatarUrl: "https://i.pravatar.cc/200?img=3",
  },
  ...SUGGESTED_PROVIDERS,
];

export function SocialUserSearchDropdown({ onClose }) {
  const navigate = useNavigate();

  const selectUser = (userId) => {
    onClose?.();
    navigate(buildSocialProfilePath(userId));
  };

  return (
    <div className="absolute left-0 right-0 top-full z-50 mt-1 overflow-hidden rounded-lg border border-header-border bg-white shadow-lg">
      <p className="border-b border-header-border px-3 py-2 text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
        Tìm user (mock)
      </p>
      <ul className="max-h-64 overflow-y-auto py-1">
        {SEARCH_USERS.map((item) => (
          <li key={item.userId}>
            <button
              type="button"
              onClick={() => selectUser(item.userId)}
              className="flex w-full items-center gap-3 px-3 py-2 text-left hover:bg-account-surface-low"
            >
              <img src={item.avatarUrl} alt="" className="h-8 w-8 rounded-full object-cover" />
              <span>
                <span className="block text-sm font-medium text-on-surface">{item.name}</span>
                <span className="block text-xs text-on-surface-variant">{item.role}</span>
              </span>
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
