export function AdminCommerceModerationPlaceholderTab({ title, description }) {
  return (
    <div className="rounded-xl border border-dashed border-outline-variant bg-surface-container-low p-10 text-center">
      <span className="material-symbols-outlined mb-3 text-4xl text-outline" aria-hidden="true">
        construction
      </span>
      <h2 className="text-headline-sm font-semibold text-on-surface">{title}</h2>
      <p className="mt-2 text-body-sm text-on-surface-variant">{description}</p>
    </div>
  );
}
