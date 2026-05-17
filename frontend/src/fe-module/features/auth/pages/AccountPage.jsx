import { EmptyState } from "../../../shared/ui/PageState";
import { getMyProfile } from "../api/authApi";
import { useAuthSession } from "../hooks/useAuthSession";

export function AccountPage() {
  const { setSession } = useAuthSession();

  const simulate401ThenRecover = async () => {
    localStorage.setItem("twohands_access_token", "expired-access-token");
    localStorage.setItem("twohands_refresh_token", "mock-refresh-b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1");
    await getMyProfile();
  };

  const simulate401ThenExpire = async () => {
    localStorage.setItem("twohands_access_token", "expired-access-token");
    localStorage.setItem("twohands_refresh_token", "mock-refresh-expired");
    await getMyProfile();
  };

  const seedAuthenticatedSession = () => {
    const userId = "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1";
    setSession({
      accessToken: `mock-access-${userId}`,
      refreshToken: `mock-refresh-${userId}`,
      user: {
        id: userId,
        email: "active@2hands.vn",
        status: "ACTIVE",
      },
    });
  };

  return (
    <section className="space-y-4">
      <header>
        <h1 className="text-2xl font-semibold text-on-surface">Tai khoan cua toi</h1>
        <p className="mt-1 text-sm text-on-surface-variant">
          Skeleton page cho ProfileAccount flow (6 tabs) theo spec.
        </p>
      </header>
      <div className="flex flex-wrap gap-3">
        <button
          type="button"
          onClick={seedAuthenticatedSession}
          className="rounded bg-primary px-3 py-2 text-sm font-semibold text-white"
        >
          Seed auth session
        </button>
        <button
          type="button"
          onClick={simulate401ThenRecover}
          className="rounded border border-outline-variant px-3 py-2 text-sm font-semibold text-on-surface"
        >
          Test 401 to refresh 200
        </button>
        <button
          type="button"
          onClick={simulate401ThenExpire}
          className="rounded border border-outline-variant px-3 py-2 text-sm font-semibold text-on-surface"
        >
          Test 401 to refresh 401
        </button>
      </div>
      <EmptyState message="Module Account tabs se duoc implement tiep theo task screen-by-screen." />
    </section>
  );
}

