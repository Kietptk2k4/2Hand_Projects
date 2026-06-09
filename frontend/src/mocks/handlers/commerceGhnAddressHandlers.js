import { delay, http, HttpResponse } from "msw";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const MOCK_PROVINCES = [
  { province_id: 202, province_name: "TP. Ho Chi Minh", code: "8" },
  { province_id: 201, province_name: "Ha Noi", code: "1" },
];

const MOCK_DISTRICTS = {
  202: [
    { district_id: 1442, province_id: 202, district_name: "Quan 1", code: "1442" },
    { district_id: 1452, province_id: 202, district_name: "Quan 3", code: "1452" },
  ],
  201: [{ district_id: 1485, province_id: 201, district_name: "Quan Ba Dinh", code: "1485" }],
};

const MOCK_WARDS = {
  1442: [{ ward_code: "20308", district_id: 1442, ward_name: "Phuong Ben Nghe" }],
  1452: [{ ward_code: "20309", district_id: 1452, ward_name: "Phuong Ben Thanh" }],
  1485: [{ ward_code: "1A0201", district_id: 1485, ward_name: "Phuong Phuc Xa" }],
};

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("COMMERCE-401", "Authentication required."), { status: 401 }),
    };
  }
  return { user };
}

export const commerceGhnAddressHandlers = [
  http.get("*/commerce/api/v1/shipping/ghn/provinces", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach tinh thanh GHN thanh cong.", { provinces: MOCK_PROVINCES }),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/shipping/ghn/districts", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const provinceId = Number(url.searchParams.get("province_id"));
    const districts = MOCK_DISTRICTS[provinceId] || [];

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach quan huyen GHN thanh cong.", { districts }),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/shipping/ghn/wards", async ({ request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const districtId = Number(url.searchParams.get("district_id"));
    const wards = MOCK_WARDS[districtId] || [];

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach phuong xa GHN thanh cong.", { wards }),
      { status: 200 },
    );
  }),
];