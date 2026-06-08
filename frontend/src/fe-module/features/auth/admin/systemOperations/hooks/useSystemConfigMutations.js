import { useCallback, useState } from "react";
import {
  createSystemConfig,
  toggleSystemConfig,
  updateSystemConfig,
} from "../api/systemConfigApi.js";
import {
  mapSystemConfigEntry,
  toCreateSystemConfigPayload,
  toToggleSystemConfigPayload,
  toUpdateSystemConfigPayload,
} from "../utils/systemConfigMapper.js";

export function useSystemConfigMutations({ onSuccess, onError }) {
  const [pending, setPending] = useState(false);

  const run = useCallback(
    async (action) => {
      setPending(true);
      try {
        const data = await action();
        onSuccess?.(data);
        return data;
      } catch (error) {
        onError?.(error);
        throw error;
      } finally {
        setPending(false);
      }
    },
    [onError, onSuccess],
  );

  const createConfig = useCallback(
    (form) =>
      run(async () => {
        const data = await createSystemConfig(toCreateSystemConfigPayload(form));
        return mapSystemConfigEntry({
          config_id: data.config_id,
          config_key: data.config_key,
          config_value: data.config_value,
          value_type: data.value_type,
          description: data.description,
          is_active: data.is_active,
          created_by: data.created_by,
          created_at: data.created_at,
          updated_by: null,
          updated_at: null,
        });
      }),
    [run],
  );

  const updateConfig = useCallback(
    (configId, form) => run(() => updateSystemConfig(configId, toUpdateSystemConfigPayload(form))),
    [run],
  );

  const toggleConfig = useCallback(
    (configId, active, reason) =>
      run(() => toggleSystemConfig(configId, toToggleSystemConfigPayload(active, reason))),
    [run],
  );

  return { pending, createConfig, updateConfig, toggleConfig };
}