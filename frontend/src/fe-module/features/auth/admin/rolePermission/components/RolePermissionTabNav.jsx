import { ROLE_PERMISSION_TABS } from "../rolePermissionTabs.js";

function NavIcon({ name, active }) {
  const className = `h-5 w-5 shrink-0 ${active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  const icons = {
    list: (
      <path
        d="M4 6h16M4 12h16M4 18h10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    assign: (
      <path
        d="M12 4v16m8-8H4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    role: (
      <path
        d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    user: (
      <path
        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
  };

  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      {icons[name]}
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

export function RolePermissionTabNav({ activeTab, onTabChange }) {
  return (
    <aside className="flex w-full shrink-0 flex-col overflow-hidden rounded-lg border border-account-surface-dim bg-account-surface shadow-sm md:w-[280px] md:min-h-[600px]">
      <div className="border-b border-account-surface-dim px-6 pb-6 pt-8 text-center">
        <h2 className="text-xl font-semibold text-on-surface">Role &amp; Permission</h2>
        <p className="mt-1 text-sm text-on-surface-variant">Quan ly vai tro va quyen truy cap</p>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-4 py-4" aria-label="Role and permission">
        {ROLE_PERMISSION_TABS.map((tab) => (
          <NavItem key={tab.id} tab={tab} isActive={activeTab === tab.id} onSelect={onTabChange} />
        ))}
      </nav>
    </aside>
  );
}
