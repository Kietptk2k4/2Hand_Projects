import { useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import { formatMetricNumber } from "../utils/modelRegistryDisplayUtils.js";

function MetricRow({ label, value }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-admin-border-subtle py-2 text-sm last:border-b-0">
      <span className="text-admin-text-secondary">{label}</span>
      <span className="font-medium tabular-nums text-admin-text">{value}</span>
    </div>
  );
}

function Section({ title, children }) {
  if (!children) return null;

  return (
    <section className="rounded-xl border border-admin-border bg-admin-surface-raised p-4">
      <h3 className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">{title}</h3>
      <div className="mt-3">{children}</div>
    </section>
  );
}

export function ModelMetricsSections({ metrics }) {
  const [showRawJson, setShowRawJson] = useState(false);

  if (!metrics) {
    return <p className="text-sm text-admin-text-muted">Không có dữ liệu metrics.</p>;
  }

  const gate = metrics.gate;
  const onnxVerify = metrics.onnx_verify;
  const baseline = metrics.baseline;
  const delta = metrics.delta;
  const featureOrder = metrics.feature_order ?? metrics.featureOrder;

  return (
    <div className="space-y-4">
      <Section title="Gate">
        <MetricRow
          label="Kết quả"
          value={
            gate?.passed === true
              ? "Đạt"
              : gate?.passed === false
                ? "Không đạt"
                : gate?.status || gate?.reason || "—"
          }
        />
        {gate?.failed?.length ? (
          <p className="mt-2 text-xs text-amber-900">
            Không đạt: {gate.failed.join(", ")}
          </p>
        ) : null}
      </Section>

      <Section title="Điểm số">
        <MetricRow label="AUC" value={formatMetricNumber(metrics.auc)} />
        <MetricRow
          label="Precision@10"
          value={formatMetricNumber(metrics.precision_at_10 ?? metrics.precisionAt10)}
        />
      </Section>

      {(baseline || delta) && (
        <Section title="So với baseline">
          {baseline ? (
            <>
              <MetricRow label="Baseline AUC" value={formatMetricNumber(baseline.auc)} />
              <MetricRow
                label="Baseline P@10"
                value={formatMetricNumber(baseline.precision_at_10 ?? baseline.precisionAt10)}
              />
            </>
          ) : null}
          {delta ? (
            <>
              <MetricRow label="Delta AUC" value={formatMetricNumber(delta.auc)} />
              <MetricRow
                label="Delta P@10"
                value={formatMetricNumber(delta.precision_at_10 ?? delta.precisionAt10)}
              />
            </>
          ) : null}
        </Section>
      )}

      {onnxVerify ? (
        <Section title="ONNX verify">
          <MetricRow
            label="Trạng thái"
            value={onnxVerify.passed === true ? "Pass" : onnxVerify.passed === false ? "Fail" : "—"}
          />
          {onnxVerify.detail ? <p className="mt-2 text-xs text-admin-text-secondary">{onnxVerify.detail}</p> : null}
        </Section>
      ) : null}

      {Array.isArray(featureOrder) && featureOrder.length > 0 ? (
        <Section title="Feature order">
          <p className="text-xs text-admin-text-secondary">{featureOrder.join(" · ")}</p>
        </Section>
      ) : null}

      <div>
        <AdminFilterButton
          type="button"
          variant="ghost"
          className="min-h-9"
          onClick={() => setShowRawJson((prev) => !prev)}
        >
          {showRawJson ? "Ẩn JSON gốc" : "Xem JSON gốc"}
        </AdminFilterButton>
        {showRawJson ? (
          <pre className="mt-3 max-h-80 overflow-auto rounded-lg bg-admin-surface-muted p-3 text-xs text-admin-text">
            {JSON.stringify(metrics, null, 2)}
          </pre>
        ) : null}
      </div>
    </div>
  );
}
