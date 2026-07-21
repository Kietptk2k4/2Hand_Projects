import {
  AdminFilterBar,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";

const SORT_OPTIONS = [
  { value: "code_asc", label: "Mã A → Z" },
  { value: "name_asc", label: "Tên A → Z" },
  { value: "updated_desc", label: "Cập nhật mới nhất" },
];

export function RoleListFilterBar({ query, sort, onQueryChange, onSortChange, resultSummary }) {
  return (
    <div className="space-y-3">
      <AdminFilterBar>
        <AdminFilterField label="Tìm kiếm" htmlFor="role-list-query" className="md:col-span-2">
          <AdminFilterInput
            id="role-list-query"
            type="search"
            placeholder="Tìm theo mã hoặc tên vai trò..."
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
          />
        </AdminFilterField>
        <AdminFilterField label="Sắp xếp" htmlFor="role-list-sort">
          <AdminFilterSelect
            id="role-list-sort"
            value={sort}
            onChange={(event) => onSortChange(event.target.value)}
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      </AdminFilterBar>
      {resultSummary ? (
        <p className="text-sm text-admin-text-secondary">{resultSummary}</p>
      ) : null}
    </div>
  );
}

export function filterAndSortRoles(roles, { query, sort }) {
  const normalizedQuery = query.trim().toLowerCase();
  let next = [...(roles || [])];

  if (normalizedQuery) {
    next = next.filter((role) => {
      const code = role.code?.toLowerCase() || "";
      const name = role.name?.toLowerCase() || "";
      return code.includes(normalizedQuery) || name.includes(normalizedQuery);
    });
  }

  next.sort((left, right) => {
    if (sort === "name_asc") {
      return (left.name || "").localeCompare(right.name || "", "vi");
    }
    if (sort === "updated_desc") {
      return new Date(right.updated_at || 0).getTime() - new Date(left.updated_at || 0).getTime();
    }
    return (left.code || "").localeCompare(right.code || "", "vi");
  });

  return next;
}
