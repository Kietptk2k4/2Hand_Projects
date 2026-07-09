import { AdminStatusBadge } from "../../../components/ui";

const STATUS_VARIANTS = {
  REQUESTED: "warning",
  APPROVED: "active",
  PAID: "success",
  REJECTED: "danger",
  CANCELLED: "neutral",
};

const STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PAID: "Đã chuyển",
  REJECTED: "Từ chối",
  CANCELLED: "Đã hủy",
};

export function PayoutStatusBadge({ status }) {
  return (
    <AdminStatusBadge variant={STATUS_VARIANTS[status] ?? "neutral"}>
      {STATUS_LABELS[status] ?? status}
    </AdminStatusBadge>
  );
}
