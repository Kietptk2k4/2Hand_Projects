import { AccountTabNav } from "./AccountTabNav.jsx";

export function AccountSettingsLayout({ activeTab, onTabChange, avatarUrl, children }) {
  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-8 px-4 py-8 md:px-8 lg:flex-row lg:items-start min-h-screen">
      <AccountTabNav activeTab={activeTab} onTabChange={onTabChange} avatarUrl={avatarUrl} />
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}
