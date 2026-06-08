export function mapSystemConfigEntry(entry) {
  if (!entry) return null;
  return {
    configId: entry.config_id,
    configKey: entry.config_key,
    configValue: entry.config_value,
    valueType: entry.value_type,
    description: entry.description,
    active: entry.is_active,
    createdBy: entry.created_by,
    createdAt: entry.created_at,
    updatedBy: entry.updated_by,
    updatedAt: entry.updated_at,
  };
}

export function mapSystemConfigsResponse(data) {
  return {
    page: data?.page ?? 1,
    size: data?.size ?? 20,
    totalElements: data?.total_elements ?? 0,
    totalPages: data?.total_pages ?? 0,
    items: (data?.items || []).map(mapSystemConfigEntry).filter(Boolean),
  };
}

export function mapSystemConfigHistoryEntry(entry) {
  if (!entry) return null;
  return {
    historyId: entry.history_id,
    configKey: entry.config_key,
    oldValue: entry.old_value,
    newValue: entry.new_value,
    changedBy: entry.changed_by,
    reason: entry.reason,
    createdAt: entry.created_at,
    valuesMasked: entry.values_masked,
  };
}

export function mapSystemConfigHistoryResponse(data) {
  return {
    configId: data?.config_id,
    configKey: data?.config_key,
    page: data?.page ?? 1,
    size: data?.size ?? 20,
    totalElements: data?.total_elements ?? 0,
    totalPages: data?.total_pages ?? 0,
    valuesMasked: data?.values_masked,
    history: (data?.history || []).map(mapSystemConfigHistoryEntry).filter(Boolean),
  };
}

export function toCreateSystemConfigPayload(form) {
  return {
    config_key: form.configKey,
    config_value: form.configValue,
    value_type: form.valueType,
    description: form.description || undefined,
    is_active: form.active,
    reason: form.reason,
  };
}

export function toUpdateSystemConfigPayload(form) {
  return {
    config_value: form.configValue,
    description: form.description || undefined,
    reason: form.reason,
  };
}

export function toToggleSystemConfigPayload(active, reason) {
  return {
    is_active: active,
    reason,
  };
}