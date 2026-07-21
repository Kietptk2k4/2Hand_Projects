import { AdminStatusBadge } from "../../components/ui";

const USER_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  PENDING_VERIFICATION: "Chờ xác minh",
  SUSPENDED: "Tạm khóa",
};

function getUserStatusVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "SUSPENDED":
      return "warning";
    case "PENDING_VERIFICATION":
      return "neutral";
    default:
      return "neutral";
  }
}

export function getRbacUserStatusLabel(status) {
  return USER_STATUS_LABELS[status] || status;
}

export function RbacUserStatusBadge({ status }) {
  return (
    <AdminStatusBadge variant={getUserStatusVariant(status)}>
      {getRbacUserStatusLabel(status)}
    </AdminStatusBadge>
  );
}
