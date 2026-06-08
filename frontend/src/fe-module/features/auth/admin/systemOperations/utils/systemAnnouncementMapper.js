export function mapSystemAnnouncementEntry(entry) {
  if (!entry) return null;
  return {
    announcementId: entry.announcement_id,
    title: entry.title,
    content: entry.content,
    severity: entry.severity,
    status: entry.status,
    pinned: entry.is_pinned,
    dismissible: entry.dismissible,
    createdBy: entry.created_by,
    createdAt: entry.created_at,
    sentAt: entry.sent_at,
  };
}

export function mapSystemAnnouncementsResponse(data) {
  return {
    page: data?.page ?? 1,
    size: data?.size ?? 20,
    totalElements: data?.total_elements ?? 0,
    totalPages: data?.total_pages ?? 0,
    items: (data?.items || []).map(mapSystemAnnouncementEntry).filter(Boolean),
  };
}

export function toCreateSystemAnnouncementPayload(form) {
  return {
    title: form.title,
    content: form.content,
    severity: form.severity,
    is_pinned: Boolean(form.pinned),
    dismissible: form.dismissible !== false,
  };
}

export function toPinSystemAnnouncementPayload(pinned) {
  return { is_pinned: Boolean(pinned) };
}

export function computeAnnouncementStats(items = []) {
  const stats = { DRAFT: 0, SENT: 0, CANCELLED: 0 };
  for (const item of items) {
    if (stats[item.status] != null) stats[item.status] += 1;
  }
  return stats;
}