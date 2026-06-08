export function SystemOperationsEmptyState({ message }) {
  return (
    <div className="rounded-lg border border-dashed border-outline-variant bg-surface-container-lowest px-6 py-10 text-center">
      <p className="text-sm text-on-surface-variant">{message}</p>
    </div>
  );
}