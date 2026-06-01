import { CommerceHomeSidebar } from "./CommerceHomeSidebar";

/** Offset for AppHeader (`h-16` = 4rem) */
const STICKY_TOP = "top-16";
const STICKY_MAX_HEIGHT = "max-h-[calc(100vh-4rem)]";

export function CommerceShell({ children, onComingSoon, showHomeSidebar = true }) {
  return (
    <div className="flex w-full items-start bg-surface-container-lowest">
      {showHomeSidebar ? (
        <div
          className={[
            "hidden shrink-0 lg:sticky lg:z-30 lg:block lg:self-start",
            STICKY_TOP,
            STICKY_MAX_HEIGHT,
          ].join(" ")}
        >
          <CommerceHomeSidebar onComingSoon={onComingSoon} />
        </div>
      ) : null}
      <div className="min-w-0 flex-1 px-4 py-8 md:px-8">{children}</div>
    </div>
  );
}
