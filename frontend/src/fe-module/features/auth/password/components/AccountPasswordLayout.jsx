import { PasswordTabNav } from "./PasswordTabNav.jsx";

export function AccountPasswordLayout({ avatarUrl, children }) {
  return (
    <div className="flex w-full flex-col gap-6 lg:flex-row lg:items-start">
      <PasswordTabNav avatarUrl={avatarUrl} />
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}
