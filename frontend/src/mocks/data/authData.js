export const mockUsers = [
  {
    id: "c0000000-0000-4000-8000-000000000099",
    email: "admin@2hands.vn",
    password: "Password123!",
    status: "ACTIVE",
    email_verified: true,
    display_name: "Admin User",
    avatar_url: "https://i.pravatar.cc/200?img=12",
    bio: "Platform administrator",
    website: "",
    social_links: {},
    is_private: false,
    appearance_mode: "SYSTEM",
    last_login_at: "2026-05-20T10:00:00Z",
    is_admin: true,
  },
  {
    id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    email: "active@2hands.vn",
    password: "Password123!",
    status: "ACTIVE",
    email_verified: true,
    display_name: "Active User",
    avatar_url: "https://i.pravatar.cc/200?img=3",
    bio: "Builder at 2Hands",
    website: "https://2hands.vn",
    social_links: {
      github: "https://github.com/active-user"
    },
    is_private: false,
    appearance_mode: "SYSTEM",
    last_login_at: "2026-05-17T08:00:00Z"
  },
  {
    id: "83e01f6e-cd36-4440-8af7-5af876ec9d95",
    email: "pending@2hands.vn",
    password: "Password123!",
    status: "PENDING_VERIFICATION",
    email_verified: false,
    display_name: "Pending User",
    avatar_url: "",
    bio: "",
    website: "",
    social_links: {},
    is_private: false,
    appearance_mode: "SYSTEM",
    last_login_at: null
  }
];

export const DEVICE_RATE_LIMIT_EMAIL = "ratelimit@2hands.vn";

const ACTIVE_USER_ID = "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1";

export const mockSessionsByUserId = {
  [ACTIVE_USER_ID]: [
    {
      id: "9cfadc7f-4aa7-4917-a076-d8a5e8bb4be6",
      device_id: "web-chrome-win11",
      ip_address: "203.113.10.20",
      user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/125.0",
      status: "ACTIVE",
      created_at: "2026-05-20T08:00:00Z",
      updated_at: "2026-05-20T08:00:00Z",
      expires_at: "2026-06-20T08:00:00Z",
    },
    {
      id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      device_id: "mobile-safari-ios",
      ip_address: "10.0.0.42",
      user_agent: "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/604.1",
      status: "ACTIVE",
      created_at: "2026-05-18T14:30:00Z",
      updated_at: "2026-05-18T14:30:00Z",
      expires_at: "2026-06-18T14:30:00Z",
    },
    {
      id: "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      device_id: "web-firefox-mac",
      ip_address: "192.168.1.105",
      user_agent: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15) Firefox/126.0",
      status: "ACTIVE",
      created_at: "2026-05-15T09:15:00Z",
      updated_at: "2026-05-15T09:15:00Z",
      expires_at: "2026-06-15T09:15:00Z",
    },
  ],
};

function buildMockLoginHistory() {
  const items = [];
  const methods = ["EMAIL", "GOOGLE", "FACEBOOK"];
  for (let i = 0; i < 28; i += 1) {
    const day = String(17 - Math.floor(i / 3)).padStart(2, "0");
    items.push({
      id: `history-${i}-${crypto.randomUUID().slice(0, 8)}`,
      login_method: methods[i % methods.length],
      ip_address: i % 2 === 0 ? "203.113.10.20" : "10.10.1.25",
      user_agent: i % 3 === 0 ? "Chrome/125.0" : "Mozilla/5.0 (Windows NT 10.0)",
      success: i % 5 !== 0,
      created_at: `2026-05-${day}T${String(9 + (i % 8)).padStart(2, "0")}:00:00Z`,
    });
  }
  return items;
}

export const mockLoginHistoryByUserId = {
  [ACTIVE_USER_ID]: buildMockLoginHistory(),
};

