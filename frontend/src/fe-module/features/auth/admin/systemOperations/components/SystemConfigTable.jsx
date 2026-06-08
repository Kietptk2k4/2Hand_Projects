import { formatDateTime } from "../../../security/utils/formatDateTime.js";

function ActiveBadge({ active }) {
  return (
    <span
      className={[
        "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold",
        active ? "bg-green-100 text-green-800" : "bg-slate-100 text-slate-600",
      ].join(" ")}
    >
      {active ? "Bật" : "Tắt"}
    </span>
  );
}

export function SystemConfigTable({ items, selectedConfigId, onSelectConfig, onOpenHistory, canUpdate }) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-outline-variant text-xs uppercase text-on-surface-variant">
          <tr>
            <th className="px-3 py-3">Key</th>
            <th className="px-3 py-3">Giá trị</th>
            <th className="px-3 py-3">Kiểu</th>
            <th className="px-3 py-3">Trạng thái</th>
            <th className="px-3 py-3">Cập nhật</th>
            <th className="px-3 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const selected = selectedConfigId === item.configId;
            return (
              <tr
                key={item.configId}
                className={[
                  "border-b border-outline-variant/60",
                  selected ? "bg-primary/5" : "hover:bg-surface-container-low",
                ].join(" ")}
              >
                <td className="px-3 py-3 font-mono text-xs">{item.configKey}</td>
                <td className="max-w-xs truncate px-3 py-3 font-mono text-xs" title={item.configValue}>
                  {item.configValue}
                </td>
                <td className="px-3 py-3">{item.valueType}</td>
                <td className="px-3 py-3">
                  <ActiveBadge active={item.active} />
                </td>
                <td className="px-3 py-3 text-xs text-on-surface-variant">
                  {item.updatedAt ? formatDateTime(item.updatedAt) : formatDateTime(item.createdAt)}
                </td>
                <td className="px-3 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => onSelectConfig?.(item.configId)}
                      className="rounded-lg border border-outline-variant px-2.5 py-1 text-xs font-medium hover:bg-surface-container-low"
                    >
                      {canUpdate ? "Sửa" : "Xem"}
                    </button>
                    <button
                      type="button"
                      onClick={() => onOpenHistory?.(item.configId)}
                      className="rounded-lg border border-outline-variant px-2.5 py-1 text-xs font-medium hover:bg-surface-container-low"
                    >
                      Lịch sử
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}