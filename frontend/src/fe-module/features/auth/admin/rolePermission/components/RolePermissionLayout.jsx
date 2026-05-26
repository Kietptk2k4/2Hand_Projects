import { RolePermissionTabNav } from "./RolePermissionTabNav.jsx";

export function RolePermissionLayout({ activeTab, onTabChange, children }) {
  return (
    <div className="flex w-full flex-col gap-6 lg:flex-row lg:items-start">
      <RolePermissionTabNav activeTab={activeTab} onTabChange={onTabChange} />
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}
