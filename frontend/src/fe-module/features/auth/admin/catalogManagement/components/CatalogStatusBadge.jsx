import { AdminStatusBadge } from "../../components/ui";

export function CatalogStatusBadge({ active }) {
  return (
    <AdminStatusBadge variant={active ? "success" : "warning"}>
      {active ? "Hoạt động" : "Vô hiệu"}
    </AdminStatusBadge>
  );
}
