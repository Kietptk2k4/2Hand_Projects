export function AuthCard({ title, subtitle, children }) {
  return (
    <section className="mx-auto w-full max-w-[520px] rounded-2xl border border-outline-variant/40 bg-surface-container p-6 sm:p-8">
      <header className="mb-6 space-y-2">
        <h1 className="text-2xl font-semibold text-on-surface">{title}</h1>
        {subtitle ? <p className="text-sm text-on-surface-variant">{subtitle}</p> : null}
      </header>
      {children}
    </section>
  );
}

