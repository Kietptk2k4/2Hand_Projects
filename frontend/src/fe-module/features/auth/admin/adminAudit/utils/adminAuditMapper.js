export function mapAdminActionLogEntry(entry) {
  if (!entry) return null;
  return {
    logId: entry.log_id,
    adminId: entry.admin_id,
    actionType: entry.action_type,
    targetType: entry.target_type,
    targetId: entry.target_id,
    status: entry.status,
    requestPayload: entry.request_payload,
    responsePayload: entry.response_payload,
    ipAddress: entry.ip_address,
    userAgent: entry.user_agent,
    createdAt: entry.created_at,
  };
}

export function mapAdminActionLogsResponse(data) {
  return {
    page: data?.page ?? 1,
    size: data?.size ?? 20,
    totalElements: data?.total_elements ?? 0,
    totalPages: data?.total_pages ?? 0,
    logs: (data?.logs || []).map(mapAdminActionLogEntry).filter(Boolean),
  };
}