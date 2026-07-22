function badgeFor(item) {
  if (item?.isActive) return { label: "active", className: "bg-emerald-100 text-emerald-800" };
  const reason = item?.metrics?.gate?.reason;
  if (reason === "rejected_by_metrics") {
    return { label: "rejected_by_metrics", className: "bg-amber-100 text-amber-900" };
  }
  return { label: "inactive", className: "bg-slate-100 text-slate-700" };
}

export function ModelRegistryTabView({
  title,
  subtitle,
  status,
  errorMessage,
  items,
  expandedVersion,
  onToggleExpand,
  onRetry,
  forbiddenMessage,
}) {
  if (status === "forbidden") {
    return (
      <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
        {forbiddenMessage || "Bạn không có quyền xem model registry."}
      </div>
    );
  }

  return (
    <section className="space-y-4">
      <header>
        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
        <p className="text-sm text-slate-600">{subtitle}</p>
      </header>

      {status === "loading" ? (
        <p className="text-sm text-slate-500">Đang tải model artifacts…</p>
      ) : null}

      {status === "error" ? (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          <p>{errorMessage}</p>
          <button type="button" className="mt-2 underline" onClick={onRetry}>
            Thử lại
          </button>
        </div>
      ) : null}

      {status === "success" && (!items || items.length === 0) ? (
        <p className="text-sm text-slate-500">Chưa có model artifact nào.</p>
      ) : null}

      {status === "success" && items?.length > 0 ? (
        <div className="overflow-x-auto rounded-lg border border-slate-200">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-3 py-2 font-medium">Version</th>
                <th className="px-3 py-2 font-medium">Format</th>
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium">Trained at</th>
                <th className="px-3 py-2 font-medium" />
              </tr>
            </thead>
            <tbody>
              {items.map((item) => {
                const badge = badgeFor(item);
                const open = expandedVersion === item.version;
                return (
                  <tr key={item.version} className="border-t border-slate-100 align-top">
                    <td className="px-3 py-2 font-medium text-slate-900">v{item.version}</td>
                    <td className="px-3 py-2 text-slate-700">{item.format}</td>
                    <td className="px-3 py-2">
                      <span className={`inline-flex rounded px-2 py-0.5 text-xs font-medium ${badge.className}`}>
                        {badge.label}
                      </span>
                    </td>
                    <td className="px-3 py-2 text-slate-600">{item.trainedAt || "—"}</td>
                    <td className="px-3 py-2">
                      <button
                        type="button"
                        className="text-xs font-medium text-slate-700 underline"
                        onClick={() => onToggleExpand(item.version)}
                      >
                        {open ? "Ẩn metrics" : "Xem metrics"}
                      </button>
                      {open ? (
                        <pre className="mt-2 max-h-64 overflow-auto rounded bg-slate-50 p-2 text-xs text-slate-800">
                          {JSON.stringify(item.metrics ?? {}, null, 2)}
                        </pre>
                      ) : null}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  );
}
