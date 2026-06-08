import { formatDateTime } from "../../auth/security/utils/formatDateTime.js";
import { useProductModerationHistory } from "../hooks/useProductModerationHistory";

export function AdminProductModerationHistoryPanel({ productId, productTitle, onBack }) {
  const { result, status, errorMessage, refetch } = useProductModerationHistory(productId);

  return (
    <div className="space-y-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h3 className="text-headline-sm font-semibold text-on-surface">Lịch sử moderation</h3>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            {productTitle || productId}
          </p>
          <p className="mt-1 font-mono text-xs text-on-surface-variant">{productId}</p>
        </div>
        <button
          type="button"
          onClick={onBack}
          className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
        >
          Quay lại danh sách
        </button>
      </div>

      {status === "loading" ? (
        <div className="space-y-2">
          {[1, 2, 3].map((key) => (
            <div key={key} className="h-14 animate-pulse rounded-lg bg-surface-container-low/70" />
          ))}
        </div>
      ) : null}

      {status === "error" ? (
        <div className="rounded-lg border border-error/30 bg-error-container/20 p-4">
          <p className="text-sm text-on-error-container">{errorMessage}</p>
          <button
            type="button"
            onClick={refetch}
            className="mt-3 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {status === "ready" && result?.history?.length ? (
        <div className="overflow-x-auto">
          <table className="min-w-[720px] w-full text-left text-sm">
            <thead>
              <tr className="border-b border-outline-variant text-on-surface-variant">
                <th className="py-3 pr-4 font-medium">Thời gian</th>
                <th className="py-3 pr-4 font-medium">Action</th>
                <th className="py-3 pr-4 font-medium">Lý do</th>
                <th className="py-3 pr-4 font-medium">Admin</th>
              </tr>
            </thead>
            <tbody>
              {result.history.map((entry) => (
                <tr key={entry.moderationLogId} className="border-b border-outline-variant/60">
                  <td className="py-3 pr-4 whitespace-nowrap">
                    {formatDateTime(entry.createdAt)}
                  </td>
                  <td className="py-3 pr-4 font-medium">{entry.action}</td>
                  <td className="py-3 pr-4">
                    <div>{entry.reason}</div>
                    {entry.note ? (
                      <div className="mt-1 text-xs text-on-surface-variant">{entry.note}</div>
                    ) : null}
                  </td>
                  <td className="py-3 pr-4 font-mono text-xs">{entry.adminId?.slice(0, 12)}…</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      {status === "ready" && !result?.history?.length ? (
        <p className="text-sm text-on-surface-variant">Chưa có lịch sử moderation cho sản phẩm này.</p>
      ) : null}
    </div>
  );
}
