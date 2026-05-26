const FOOTER_LINKS = [
  { label: "Privacy Policy", href: "#privacy" },
  { label: "Terms of Service", href: "#terms" },
  { label: "Support", href: "#support" },
  { label: "Contact Us", href: "#contact" },
];

export function AppFooter({ className = "" }) {
  return (
    <footer
      className={[
        "mt-auto w-full bg-footer-surface px-6 py-10 sm:px-[10%]",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="mx-auto flex w-full max-w-[1280px] flex-col items-center justify-between gap-6 md:flex-row md:items-center">
        <div className="flex flex-col items-center gap-2 md:items-start">
          <p className="text-2xl font-bold text-footer-brand sm:text-3xl">2Hands</p>
          <p className="text-center text-sm text-footer-muted md:text-left">
            © 2024 2Hands. Professional Service Marketplace.
          </p>
        </div>

        <nav
          className="flex flex-wrap items-center justify-center gap-6 text-sm text-footer-muted"
          aria-label="Footer"
        >
          {FOOTER_LINKS.map((link) => (
            <a
              key={link.label}
              href={link.href}
              className="transition-colors hover:text-footer-brand"
            >
              {link.label}
            </a>
          ))}
        </nav>
      </div>
    </footer>
  );
}
