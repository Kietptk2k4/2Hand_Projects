import { fetchGhnDistricts, fetchGhnProvinces, fetchGhnWards } from "../api/ghnAddressApi";
import {
  mapGhnDistrictOptions,
  mapGhnProvinceOptions,
  mapGhnWardOptions,
} from "./ghnAddressMapper";
import {
  cacheGhnDistrictOptions,
  cacheGhnProvinceOptions,
  cacheGhnWardOptions,
} from "./ghnAddressLabelCache";

function parsePositiveInt(value) {
  const parsed = Number.parseInt(String(value), 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export async function prefetchGhnAddressLabelsForAddresses(addresses = []) {
  if (!addresses.length) return;

  try {
    const provincesRaw = await fetchGhnProvinces();
    const provinceOptions = mapGhnProvinceOptions(provincesRaw);
    cacheGhnProvinceOptions(provinceOptions);

    const provinceIds = [
      ...new Set(
        addresses.map((item) => parsePositiveInt(item.provinceCode)).filter(Boolean)
      ),
    ];

    const districtIdsToLoad = new Set(
      addresses.map((item) => parsePositiveInt(item.districtCode)).filter(Boolean)
    );

    await Promise.all(
      provinceIds.map(async (provinceId) => {
        const districtsRaw = await fetchGhnDistricts(provinceId);
        const districtOptions = mapGhnDistrictOptions(districtsRaw);
        cacheGhnDistrictOptions(districtOptions);
      })
    );

    await Promise.all(
      [...districtIdsToLoad].map(async (districtId) => {
        const wardsRaw = await fetchGhnWards(districtId);
        const wardOptions = mapGhnWardOptions(wardsRaw);
        cacheGhnWardOptions(wardOptions);
      })
    );
  } catch {
    // Display falls back to raw codes when GHN master data is unavailable.
  }
}