import { ACCOUNT_TABS } from "../accountTabs.js";

const DEFAULT_AVATAR = "https://i.pravatar.cc/160?img=11";

function NavIcon({ name, active, danger }) {
  const stroke = danger ? "currentColor" : active ? "#0066ff" : "currentColor";
  const className = `h-5 w-5 shrink-0 ${danger ? "text-account-danger" : active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  const paths = {
    account: (
      <path
        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    edit: (
      <path
        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    avatar: (
      <path
        d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    privacy: (
      <path
        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    settings: (
      <>
        <path
          d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" strokeLinecap="round" strokeLinejoin="round" />
      </>
    ),
    notifications: (
      <>
        <path
          d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path d="M13.73 21a2 2 0 01-3.46 0" strokeLinecap="round" strokeLinejoin="round" />
      </>
    ),
    delete: (
      <path
        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
  };

  return (
    <svg className={className} fill="none" stroke={stroke} strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      {paths[name]}
    </svg>
  );
}

function NavItem({ tab, isActive, onSelect }) {
  const isDanger = Boolean(tab.danger);

  if (isDanger) {
    return (
      <button
        type="button"
        onClick={() => onSelect(tab.id)}
        className={[
          "group flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition-colors",
          isActive ? "bg-red-50 text-account-danger" : "text-account-danger hover:bg-red-50",
        ].join(" ")}
      >
        <NavIcon name={tab.icon} active={isActive} danger />
        <span className="text-sm font-medium">{tab.labelVn}</span>
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={() => onSelect(tab.id)}
      className={[
        "group relative flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition-colors",
        isActive
          ? "bg-account-surface-low font-semibold text-primary"
          : "text-on-surface-variant hover:bg-account-surface-low hover:text-primary",
      ].join(" ")}
    >
      <NavIcon name={tab.icon} active={isActive} />
      <span className="text-sm">{tab.labelVn}</span>
      {isActive ? (
        <span className="absolute right-0 top-1/2 h-8 w-1 -translate-y-1/2 rounded-l-full bg-primary" aria-hidden="true" />
      ) : null}
    </button>
  );
}

export function AccountTabNav({ activeTab, onTabChange, avatarUrl }) {
  const mainTabs = ACCOUNT_TABS.filter((tab) => !tab.danger);
  const deleteTab = ACCOUNT_TABS.find((tab) => tab.danger);

  return (
    <aside className="flex w-full shrink-0 flex-col overflow-hidden rounded-lg border border-account-surface-dim bg-account-surface shadow-sm md:w-[280px] md:min-h-[720px]">
      <div className="flex flex-col items-center px-6 pb-6 pt-8 text-center">
        <img
          src={avatarUrl || DEFAULT_AVATAR}
          alt=""
          className="mb-4 h-20 w-20 rounded-full border-2 border-white object-cover shadow-sm"
        />
        <h2 className="text-xl font-semibold text-on-surface">Cài đặt hồ sơ</h2>
        <p className="mt-1 text-sm text-on-surface-variant">Quản lý tài khoản 2Hands của bạn</p>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-4" aria-label="Cài đặt tài khoản">
        {mainTabs.map((tab) => (
          <NavItem key={tab.id} tab={tab} isActive={activeTab === tab.id} onSelect={onTabChange} />
        ))}
      </nav>

      {deleteTab ? (
        <div className="mt-auto border-t border-account-surface-dim p-6">
          <NavItem tab={deleteTab} isActive={activeTab === deleteTab.id} onSelect={onTabChange} />
        </div>
      ) : null}
    </aside>
  );
}
