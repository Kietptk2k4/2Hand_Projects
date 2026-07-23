import { formatPostListDateTime } from "../../contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import {
  deriveArtifactStatus,
  getArtifactStatusBadgeClass,
  getArtifactStatusLabel,
  summarizeMetrics,
} from "../utils/modelRegistryDisplayUtils.js";

function TrainedAtCell({ value }) {
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function StatusBadge({ item }) {
  const status = deriveArtifactStatus(item);
  return (
    <span
      className={`inline-flex rounded px-2 py-0.5 text-xs font-medium ${getArtifactStatusBadgeClass(status)}`}
    >
      {getArtifactStatusLabel(status)}
    </span>
  );
}

function ArtifactMobileCard({ item, selected, onRowSelect }) {
  const summary = summarizeMetrics(item);

  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onRowSelect?.(item)}
      ariaLabel={`Model phiên bản ${item.version}`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <p className="font-mono font-medium text-admin-text">v{item.version}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <StatusBadge item={item} />
            <span className="text-xs text-admin-text-secondary">{item.format}</span>
          </div>
          <p className="mt-2 text-xs tabular-nums text-admin-text-muted">
            AUC {summary.auc} · P@10 {summary.precisionAt10}
          </p>
          <div className="mt-2">
            <TrainedAtCell value={item.trainedAt} />
          </div>
        </div>
        <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
          chevron_right
        </span>
      </div>
    </AdminMobileCard>
  );
}

export function ModelRegistryTable({ items, selectedVersion, onRowSelect }) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {items.map((item) => (
          <ArtifactMobileCard
            key={item.version}
            item={item}
            selected={String(selectedVersion) === String(item.version)}
            onRowSelect={onRowSelect}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="760px" ariaLabel="Danh sách phiên bản model">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Phiên bản</AdminDataTableCell>
            <AdminDataTableCell header>Định dạng</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Huấn luyện</AdminDataTableCell>
            <AdminDataTableCell header>AUC</AdminDataTableCell>
            <AdminDataTableCell header>P@10</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const selected = String(selectedVersion) === String(item.version);
            const summary = summarizeMetrics(item);

            return (
              <AdminDataTableRow
                key={item.version}
                interactive
                selected={selected}
                onClick={() => onRowSelect?.(item)}
              >
                <AdminDataTableCell>
                  <span className="font-mono font-medium text-admin-text">v{item.version}</span>
                </AdminDataTableCell>
                <AdminDataTableCell>{item.format}</AdminDataTableCell>
                <AdminDataTableCell>
                  <StatusBadge item={item} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <TrainedAtCell value={item.trainedAt} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="tabular-nums text-admin-text-secondary">{summary.auc}</span>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="tabular-nums text-admin-text-secondary">{summary.precisionAt10}</span>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
