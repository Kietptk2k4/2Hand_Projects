export function mapAdminUserListItemToInvestigationTarget(row) {
  if (!row) return null;
  return {
    user_id: row.id,
    email: row.email,
    display_name: row.display_name,
    status: row.status,
    role_codes: row.role_codes || [],
  };
}
