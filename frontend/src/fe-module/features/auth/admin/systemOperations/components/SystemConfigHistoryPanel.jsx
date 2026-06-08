import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { GENERIC_RETRY } from "../constants/systemOperationsUiStrings.js";
import { useSystemConfigHistory } from "../hooks/useSystemConfigHistory.js";

export function SystemConfigHistoryPanel({ configId, enabled }) {
  const { result, status, errorMessage, refetch } = useSystemConfigHistory({
    configId,
    enabled,
  });

  if (!configId) return null;

  if (status === "loading") return <AccountSkeleton />;

  if (status === "error") {
    return (
      <div className="space-y-3">
        <ErrorState message={errorMessage} />
        <button
          type="button"
          onClick={refetch}
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
        >
          {GENERIC_RETRY}
        </button>
      </div>
    );
  }

  if (!result?.history?.length) {
    return <p className="text-sm text-on-surface-variant">Chưa có lịch sử thay đổi.</p>;
  }

  return (
    <div className="space-y-4">
      {result.valuesMasked ? (
        <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-900">
          Một số giá trị đã được che vì lý do bảo mật.
        </p>
      ) : null}
      <ul className="space-y-3">
        {result.history.map((entry) => (
          <li key={entry.historyId} className="rounded-lg border border-outline-variant p-4">
            <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-on-surface-variant">
              <span>{formatDateTime(entry.createdAt)}</span>
              <span className="font-mono">{entry.changedBy || "-"}</span>
            </div>
            <p className="mt-2 text-sm">
              <span className="text-on-surface-variant">Cũ:</span>{" "}
              <code className="text-xs">{entry.oldValue ?? "-"}</code>
            </p>
            <p className="mt-1 text-sm">
              <span className="text-on-surface-variant">Mới:</span>{" "}
              <code className="text-xs">{entry.newValue ?? "-"}</code>
            </p>
            {entry.reason ? (
              <p className="mt-2 text-xs text-on-surface-variant">Lý do: {entry.reason}</p>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  );
}