import { ADMIN_TOP_TABS } from "../adminTabs.js";
import { ADMIN_AUDIT_TABS } from "../adminAudit/adminAuditTabs.js";
import { CONTENT_MODERATION_TABS } from "../contentModeration/contentModerationTabs.js";
import { ORDER_SUPPORT_TABS } from "../orderSupport/orderSupportTabs.js";
import { USER_INVESTIGATION_TABS } from "../userInvestigation/userInvestigationTabs.js";
import { ROLE_PERMISSION_TABS } from "../rolePermission/rolePermissionTabs.js";

const SECTION_CHILDREN = {
  rolePermission: ROLE_PERMISSION_TABS,
  userInvestigation: USER_INVESTIGATION_TABS,
  adminAudit: ADMIN_AUDIT_TABS,
  contentModeration: CONTENT_MODERATION_TABS,
  orderSupport: ORDER_SUPPORT_TABS,
};

function ParentNavIcon({ sectionId, active }) {
  const className = `h-5 w-5 shrink-0 ${active ? "text-primary" : "text-on-surface-variant group-hover:text-primary"}`;

  if (sectionId === "userInvestigation") {
    return (
      <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
        <path
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  }

  if (sectionId === "adminAudit") {
    return (
      <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
        <path
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  }

  if (sectionId === "contentModeration") {
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

  if (sectionId === "orderSupport") {
    return (
      <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
        <path
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
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
    revoke: (
      <path
        d="M15 12H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"
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
    profile: (
      <path
        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    history: <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" strokeLinecap="round" strokeLinejoin="round" />,
    enforcement: (
      <path
        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    timeline: (
      <path
        d="M4 6h16M4 10h16M4 14h10M4 18h8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    sessions: (
      <path
        d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    shop: (
      <path
        d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    review: (
      <path
        d="M7 8h10M7 12h6m-6 4h8M5 4h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    product: (
      <path
        d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    order: (
      <path
        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    payment: (
      <path
        d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    shipment: (
      <path
        d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10m10 0h4m-4 0a2 2 0 104 0m-6 0a2 2 0 11-4 0m8 0V8h3l3 3v5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    webhook: (
      <path
        d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    audit: (
      <path
        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    post: (
      <path
        d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 12h10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    ),
    comment: (
      <path
        d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
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
