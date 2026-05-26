import { AccountTabNav } from "./AccountTabNav.jsx";

export function AccountSettingsLayout({ activeTab, onTabChange, avatarUrl, children }) {
  return (
    <div className="flex w-full flex-col gap-6 lg:flex-row lg:items-start">
      <AccountTabNav activeTab={activeTab} onTabChange={onTabChange} avatarUrl={avatarUrl} />
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}
