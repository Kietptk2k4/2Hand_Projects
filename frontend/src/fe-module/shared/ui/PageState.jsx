export function LoadingState({ message = "Dang tai du lieu..." }) {
  return (
    <div className="rounded-2xl border border-outline-variant/40 bg-surface-container p-6 text-sm text-on-surface">
      {message}
    </div>
  );
}

export function ErrorState({ message = "Da xay ra loi. Vui long thu lai." }) {
  return (
    <div className="rounded-2xl border border-error/30 bg-error-container/40 p-6 text-sm text-on-error-container">
      {message}
    </div>
  );
}

export function EmptyState({ message = "Chua co du lieu." }) {
  return (
    <div className="rounded-2xl border border-outline-variant/40 bg-surface-container p-6 text-sm text-on-surface-variant">
      {message}
    </div>
  );
}

