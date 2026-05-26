import { PASSWORD_TABS } from "../passwordTabs.js";

const DEFAULT_AVATAR = "https://i.pravatar.cc/160?img=11";
const SINGLE_TAB = PASSWORD_TABS[0];

function PasswordNavIcon({ active }) {
  const className = `h-5 w-5 shrink-0 ${active ? "text-primary" : "text-on-surface-variant"}`;

  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function PasswordTabNav({ avatarUrl }) {
  return (
    <aside className="flex w-full shrink-0 flex-col overflow-hidden rounded-lg border border-account-surface-dim bg-account-surface shadow-sm md:w-[280px] md:min-h-[480px]">
      <div className="flex flex-col items-center px-6 pb-6 pt-8 text-center">
        <img
          src={avatarUrl || DEFAULT_AVATAR}
          alt=""
          className="mb-4 h-20 w-20 rounded-full border-2 border-white object-cover shadow-sm"
        />
        <h2 className="text-xl font-semibold text-on-surface">Mat khau &amp; bao mat</h2>
        <p className="mt-1 text-sm text-on-surface-variant">Cap nhat mat khau dang nhap</p>
      </div>

      <nav className="px-4 pb-6" aria-label="Account password">
        <div className="relative flex w-full items-center gap-3 rounded-lg bg-account-surface-low px-4 py-3 font-semibold text-primary">
          <PasswordNavIcon active />
          <span className="text-sm">{SINGLE_TAB.labelVn}</span>
          <span className="absolute right-0 top-1/2 h-8 w-1 -translate-y-1/2 rounded-l-full bg-primary" aria-hidden="true" />
        </div>
      </nav>
    </aside>
  );
}
