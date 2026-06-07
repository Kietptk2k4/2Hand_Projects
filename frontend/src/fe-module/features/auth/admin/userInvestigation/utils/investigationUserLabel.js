export function getInvestigationUserLabel(userId, profile, selectedUser) {
  if (profile?.display_name && profile?.email) {
    return `${profile.display_name} (${profile.email})`;
  }
  if (profile?.email) return profile.email;
  if (selectedUser?.display_name && selectedUser?.email) {
    return `${selectedUser.display_name} (${selectedUser.email})`;
  }
  if (selectedUser?.email) return selectedUser.email;
  return userId;
}
