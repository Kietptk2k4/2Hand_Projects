import { SECURITY_TABS } from "../securityTabs.js";

const DEFAULT_AVATAR = "https://i.pravatar.cc/160?img=11";

function NavIcon({ name, active }) {
  const className = `h-5 w-5 shrink-0 ${active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  if (name === "sessions") {
    return (
      <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
        <path
          d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  }

  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function NavItem({ tab, isActive, onSelect }) {
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

export function SecurityTabNav({ activeTab, onTabChange, avatarUrl }) {
  return (
    <aside className="flex w-full shrink-0 flex-col overflow-hidden rounded-lg border border-account-surface-dim bg-account-surface shadow-sm md:w-[280px] md:min-h-[520px]">
      <div className="flex flex-col items-center px-6 pb-6 pt-8 text-center">
        <img
          src={avatarUrl || DEFAULT_AVATAR}
          alt=""
          className="mb-4 h-20 w-20 rounded-full border-2 border-white object-cover shadow-sm"
        />
        <h2 className="text-xl font-semibold text-on-surface">Bảo mật tài khoản</h2>
        <p className="mt-1 text-sm text-on-surface-variant">Quan ly phiên và lịch sử đăng nhập</p>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-4 pb-6" aria-label="Account security">
        {SECURITY_TABS.map((tab) => (
          <NavItem key={tab.id} tab={tab} isActive={activeTab === tab.id} onSelect={onTabChange} />
        ))}
      </nav>
    </aside>
  );
}
