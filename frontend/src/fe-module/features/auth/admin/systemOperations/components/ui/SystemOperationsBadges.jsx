import { AdminStatusBadge } from "../../../components/ui";
import {
  ANNOUNCEMENT_SEVERITY_LABELS,
  ANNOUNCEMENT_STATUS_LABELS,
} from "../../constants/announcementListConstants.js";

export function ConfigActiveBadge({ active }) {
  return (
    <AdminStatusBadge variant={active ? "success" : "neutral"}>
      {active ? "Bật" : "Tắt"}
    </AdminStatusBadge>
  );
}

const ANNOUNCEMENT_STATUS_VARIANTS = {
  DRAFT: "neutral",
  SENT: "success",
  CANCELLED: "danger",
};

const ANNOUNCEMENT_SEVERITY_VARIANTS = {
  INFO: "active",
  WARNING: "warning",
  CRITICAL: "danger",
};

export function AnnouncementStatusBadge({ status }) {
  return (
    <AdminStatusBadge variant={ANNOUNCEMENT_STATUS_VARIANTS[status] ?? "neutral"}>
      {ANNOUNCEMENT_STATUS_LABELS[status] || status}
    </AdminStatusBadge>
  );
}

export function AnnouncementSeverityBadge({ severity }) {
  return (
    <AdminStatusBadge variant={ANNOUNCEMENT_SEVERITY_VARIANTS[severity] ?? "neutral"}>
      {ANNOUNCEMENT_SEVERITY_LABELS[severity] || severity}
    </AdminStatusBadge>
  );
}
