import { ADMIN_TOP_TABS } from "../adminTabs.js";
import { LOGIN_SESSION_TABS } from "../loginSession/loginSessionTabs.js";
import { ROLE_PERMISSION_TABS } from "../rolePermission/rolePermissionTabs.js";

const SECTION_CHILDREN = {
  rolePermission: ROLE_PERMISSION_TABS,
  loginSession: LOGIN_SESSION_TABS,
};

function ParentNavIcon({ sectionId, active }) {
  const className = `h-5 w-5 shrink-0 ${active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  if (sectionId === "loginSession") {
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
      <path
        d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function ChildNavIcon({ name, active }) {
  const className = `h-4 w-4 shrink-0 ${active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  const icons = {
    list: <path d="M4 6h16M4 12h16M4 18h10" strokeLinecap="round" strokeLinejoin="round" />,
    assign: <path d="M12 4v16m8-8H4" strokeLinecap="round" strokeLinejoin="round" />,
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
    history: <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" strokeLinecap="round" strokeLinejoin="round" />,
    sessions: (
      <path
        d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
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

function ParentNavItem({ section, isExpanded, onSelect }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(section.id)}
      aria-expanded={isExpanded}
      className={[
        "group relative flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition-colors",
        isExpanded
          ? "bg-account-surface-low font-semibold text-primary"
          : "text-on-surface-variant hover:bg-account-surface-low hover:text-primary",
      ].join(" ")}
    >
      <ParentNavIcon sectionId={section.id} active={isExpanded} />
      <span className="flex-1 text-sm">{section.labelVn}</span>
      <svg
        className={[
          "h-4 w-4 shrink-0 transition-transform",
          isExpanded ? "rotate-180 text-primary" : "text-on-surface-variant",
        ].join(" ")}
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M19 9l-7 7-7-7" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      {isExpanded ? (
        <span className="absolute right-0 top-1/2 h-8 w-1 -translate-y-1/2 rounded-l-full bg-primary" aria-hidden="true" />
      ) : null}
    </button>
  );
}

function ChildNavItem({ tab, isActive, onSelect }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(tab.id)}
      className={[
        "group relative flex w-full items-center gap-2 rounded-lg py-2.5 pl-10 pr-4 text-left transition-colors",
        isActive
          ? "bg-account-surface-low font-semibold text-primary"
          : "text-on-surface-variant hover:bg-account-surface-low hover:text-primary",
      ].join(" ")}
    >
      <ChildNavIcon name={tab.icon} active={isActive} />
      <span className="text-sm">{tab.labelVn}</span>
      {isActive ? (
        <span className="absolute right-0 top-1/2 h-6 w-1 -translate-y-1/2 rounded-l-full bg-primary" aria-hidden="true" />
      ) : null}
    </button>
  );
}

export function AdminNestedNav({ activeSection, activeChildTab, onSectionChange, onChildTabChange }) {
  return (
    <aside className="flex w-full shrink-0 flex-col overflow-hidden rounded-lg border border-account-surface-dim bg-account-surface shadow-sm md:w-[280px] md:min-h-[600px]">
      <div className="border-b border-account-surface-dim px-6 pb-6 pt-8 text-center">
        <h1 className="text-xl font-semibold text-on-surface">Quan tri</h1>
        <p className="mt-1 text-sm text-on-surface-variant">Administration</p>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-4 py-4" aria-label="Admin navigation">
        {ADMIN_TOP_TABS.map((section) => {
          const isExpanded = activeSection === section.id;
          const children = SECTION_CHILDREN[section.id] || [];

          return (
            <div key={section.id} className="flex flex-col gap-0.5">
              <ParentNavItem section={section} isExpanded={isExpanded} onSelect={onSectionChange} />
              {isExpanded
                ? children.map((child) => (
                    <ChildNavItem
                      key={child.id}
                      tab={child}
                      isActive={activeChildTab === child.id}
                      onSelect={onChildTabChange}
                    />
                  ))
                : null}
            </div>
          );
        })}
      </nav>
    </aside>
  );
}
