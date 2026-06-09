import { useCallback, useEffect, useState } from "react";
import { fetchGhnDistricts, fetchGhnProvinces, fetchGhnWards } from "../api/ghnAddressApi";
import {
  mapGhnDistrictOptions,
  mapGhnProvinceOptions,
  mapGhnWardOptions,
} from "../utils/ghnAddressMapper";
import {
  cacheGhnDistrictOptions,
  cacheGhnProvinceOptions,
  cacheGhnWardOptions,
} from "../utils/ghnAddressLabelCache";

function parsePositiveInt(value) {
  const parsed = Number.parseInt(String(value), 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function useGhnAddressOptions({ provinceCode, districtCode, enabled = true }) {
  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [wards, setWards] = useState([]);
  const [isLoadingProvinces, setIsLoadingProvinces] = useState(false);
  const [isLoadingDistricts, setIsLoadingDistricts] = useState(false);
  const [isLoadingWards, setIsLoadingWards] = useState(false);
  const [loadError, setLoadError] = useState("");

  const loadProvinces = useCallback(async () => {
    if (!enabled) return;

    setIsLoadingProvinces(true);
    setLoadError("");
    try {
      const raw = await fetchGhnProvinces();
      const options = mapGhnProvinceOptions(raw);
      cacheGhnProvinceOptions(options);
      setProvinces(options);
    } catch (error) {
      setProvinces([]);
      setLoadError(error?.message || "Không thể tải danh sách tỉnh/thành.");
    } finally {
      setIsLoadingProvinces(false);
    }
  }, [enabled]);

  const loadDistricts = useCallback(
    async (provinceId) => {
      if (!enabled || !provinceId) {
        setDistricts([]);
        return;
      }

      setIsLoadingDistricts(true);
      setLoadError("");
      try {
        const raw = await fetchGhnDistricts(provinceId);
        const options = mapGhnDistrictOptions(raw);
        cacheGhnDistrictOptions(options);
        setDistricts(options);
      } catch (error) {
        setDistricts([]);
        setLoadError(error?.message || "Không thể tải danh sách quận/huyện.");
      } finally {
        setIsLoadingDistricts(false);
      }
    },
    [enabled],
  );

  const loadWards = useCallback(
    async (districtId) => {
      if (!enabled || !districtId) {
        setWards([]);
        return;
      }

      setIsLoadingWards(true);
      setLoadError("");
      try {
        const raw = await fetchGhnWards(districtId);
        const options = mapGhnWardOptions(raw);
        cacheGhnWardOptions(options);
        setWards(options);
      } catch (error) {
        setWards([]);
        setLoadError(error?.message || "Không thể tải danh sách phường/xã.");
      } finally {
        setIsLoadingWards(false);
      }
    },
    [enabled],
  );

  useEffect(() => {
    loadProvinces();
  }, [loadProvinces]);

  useEffect(() => {
    const provinceId = parsePositiveInt(provinceCode);
    if (!provinceId) {
      setDistricts([]);
      return;
    }
    loadDistricts(provinceId);
  }, [provinceCode, loadDistricts]);

  useEffect(() => {
    const districtId = parsePositiveInt(districtCode);
    if (!districtId) {
      setWards([]);
      return;
    }
    loadWards(districtId);
  }, [districtCode, loadWards]);

  const retry = useCallback(() => {
    const provinceId = parsePositiveInt(provinceCode);
    const districtId = parsePositiveInt(districtCode);

    loadProvinces().then(() => {
      if (provinceId) {
        return loadDistricts(provinceId).then(() => {
          if (districtId) {
            return loadWards(districtId);
          }
          return undefined;
        });
      }
      return undefined;
    });
  }, [districtCode, loadDistricts, loadProvinces, loadWards, provinceCode]);

  return {
    provinces,
    districts,
    wards,
    isLoadingProvinces,
    isLoadingDistricts,
    isLoadingWards,
    isLoading: isLoadingProvinces || isLoadingDistricts || isLoadingWards,
    loadError,
    retry,
  };
}