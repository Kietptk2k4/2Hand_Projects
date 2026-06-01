import { CommerceHomeSidebar } from "./CommerceHomeSidebar";

export function CommerceShell({ children, onComingSoon, showHomeSidebar = true }) {
  return (
    <div className="flex w-full bg-surface-container-lowest">
      {showHomeSidebar ? <CommerceHomeSidebar onComingSoon={onComingSoon} /> : null}
      <div className="flex-1 px-4 py-8 md:px-8">{children}</div>
    </div>
  );
}
