import { formatDateTime } from "../../../security/utils/formatDateTime.js";

function StatusBadge({ status }) {
  const tones = {
    DRAFT: "bg-slate-100 text-slate-700",
    SENT: "bg-green-100 text-green-800",
    CANCELLED: "bg-red-100 text-red-800",
  };
  return (
    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${tones[status] || "bg-slate-100"}`}>
      {status}
    </span>
  );
}

function SeverityBadge({ severity }) {
  const tones = {
    INFO: "text-blue-700",
    WARNING: "text-amber-700",
    CRITICAL: "text-red-700",
  };
  return <span className={`text-xs font-semibold ${tones[severity] || ""}`}>{severity}</span>;
}

export function SystemAnnouncementTable({ items, onActionRequest }) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-outline-variant text-xs uppercase text-on-surface-variant">
          <tr>
            <th className="px-3 py-3">Tiêu đề</th>
            <th className="px-3 py-3">Mức độ</th>
            <th className="px-3 py-3">Trạng thái</th>
            <th className="px-3 py-3">Pin</th>
            <th className="px-3 py-3">Gửi lúc</th>
            <th className="px-3 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.announcementId} className="border-b border-outline-variant/60 hover:bg-surface-container-low">
              <td className="px-3 py-3">
                <p className="font-medium text-on-surface">{item.title}</p>
                <p className="mt-1 line-clamp-2 text-xs text-on-surface-variant">{item.content}</p>
              </td>
              <td className="px-3 py-3">
                <SeverityBadge severity={item.severity} />
              </td>
              <td className="px-3 py-3">
                <StatusBadge status={item.status} />
              </td>
              <td className="px-3 py-3 text-xs">{item.pinned ? "Có" : "Không"}</td>
              <td className="px-3 py-3 text-xs text-on-surface-variant">
                {item.sentAt ? formatDateTime(item.sentAt) : "-"}
              </td>
              <td className="px-3 py-3">
                <div className="flex flex-wrap justify-end gap-2">
                  {item.status === "DRAFT" ? (
                    <button
                      type="button"
                      onClick={() => onActionRequest?.({ type: "publish", item })}
                      className="rounded-lg bg-primary px-2.5 py-1 text-xs font-semibold text-white"
                    >
                      Publish
                    </button>
                  ) : null}
                  {item.status === "SENT" ? (
                    <button
                      type="button"
                      onClick={() =>
                        onActionRequest?.({ type: item.pinned ? "unpin" : "pin", item })
                      }
                      className="rounded-lg border border-outline-variant px-2.5 py-1 text-xs font-medium"
                    >
                      {item.pinned ? "Bỏ pin" : "Pin"}
                    </button>
                  ) : null}
                  {item.status !== "CANCELLED" ? (
                    <button
                      type="button"
                      onClick={() => onActionRequest?.({ type: "cancel", item })}
                      className="rounded-lg border border-error/40 px-2.5 py-1 text-xs font-medium text-error"
                    >
                      Hủy
                    </button>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}