/** Fixed UUIDs for stable MSW tests */
export const ROLE_IDS = {
  ADMIN: "a0000000-0000-4000-8000-000000000001",
  MODERATOR: "a0000000-0000-4000-8000-000000000002",
  USER: "a0000000-0000-4000-8000-000000000003",
};

export const ADMIN_USER_ID = "c0000000-0000-4000-8000-000000000099";

export const mockRoles = [
  {
    id: ROLE_IDS.ADMIN,
    code: "ADMIN",
    name: "Administrator",
    created_at: "2026-01-10T08:00:00Z",
    updated_at: "2026-05-17T10:00:00Z",
  },
  {
    id: ROLE_IDS.MODERATOR,
    code: "MODERATOR",
    name: "Moderator",
    created_at: "2026-01-10T08:00:00Z",
    updated_at: "2026-05-15T12:00:00Z",
  },
  {
    id: ROLE_IDS.USER,
    code: "USER",
    name: "Standard User",
    created_at: "2026-01-10T08:00:00Z",
    updated_at: "2026-05-10T09:00:00Z",
  },
];

export const mockPermissionsCatalog = [
  { code: "ASSIGN_ROLE", description: "Assign roles to users" },
  { code: "USER_READ", description: "Read user information" },
  { code: "USER_UPDATE", description: "Update user information" },
  { code: "USER_DELETE", description: "Soft delete user accounts" },
  { code: "ROLE_READ", description: "View roles and permissions" },
];

export const mockRolePermissionsByRoleId = {
  [ROLE_IDS.ADMIN]: [
    { code: "ASSIGN_ROLE", description: "Assign roles to users" },
    { code: "USER_READ", description: "Read user information" },
    { code: "USER_UPDATE", description: "Update user information" },
    { code: "USER_DELETE", description: "Soft delete user accounts" },
    { code: "ROLE_READ", description: "View roles and permissions" },
  ],
  [ROLE_IDS.MODERATOR]: [
    { code: "USER_READ", description: "Read user information" },
    { code: "USER_UPDATE", description: "Update user information" },
    { code: "ROLE_READ", description: "View roles and permissions" },
  ],
  [ROLE_IDS.USER]: [{ code: "USER_READ", description: "Read user information" }],
};

/** userId -> roleId */
export const mockUserRoleAssignments = {
  "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1": ROLE_IDS.USER,
  [ADMIN_USER_ID]: ROLE_IDS.ADMIN,
  "83e01f6e-cd36-4440-8af7-5af876ec9d95": ROLE_IDS.USER,
};

function permissionsForRoleId(roleId) {
  const codes = mockRolePermissionsByRoleId[roleId] || [];
  return codes.map((p) => ({ code: p.code }));
}

export const mockUserPermissionsByUserId = Object.fromEntries(
  Object.entries(mockUserRoleAssignments).map(([userId, roleId]) => [
    userId,
    permissionsForRoleId(roleId),
  ])
);

export const mockAssignableUsers = [
  {
    id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    email: "active@2hands.vn",
    display_name: "Active User",
  },
  {
    id: "83e01f6e-cd36-4440-8af7-5af876ec9d95",
    email: "pending@2hands.vn",
    display_name: "Pending User",
  },
  {
    id: ADMIN_USER_ID,
    email: "admin@2hands.vn",
    display_name: "Admin User",
  },
];
