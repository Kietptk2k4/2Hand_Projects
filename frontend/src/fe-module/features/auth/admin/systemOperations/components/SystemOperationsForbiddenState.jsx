export function SystemOperationsForbiddenState({ message }) {
  return (
    <div className="rounded-lg border border-error/30 bg-error-container/30 px-6 py-8 text-center">
      <p className="text-sm text-on-error-container">{message}</p>
    </div>
  );
}