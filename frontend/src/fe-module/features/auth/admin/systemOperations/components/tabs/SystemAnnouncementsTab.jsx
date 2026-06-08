import { useMemo, useState } from "react";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { ANNOUNCEMENT_PAGE_SIZE } from "../../constants/systemAnnouncementConstants.js";
import {
  SYSTEM_ANNOUNCEMENTS_EMPTY,
  SYSTEM_ANNOUNCEMENTS_FORBIDDEN,
  SYSTEM_ANNOUNCEMENTS_SUBTITLE,
  SYSTEM_ANNOUNCEMENTS_TITLE,
  GENERIC_RETRY,
} from "../../constants/systemOperationsUiStrings.js";
import { useSystemAnnouncementMutations } from "../../hooks/useSystemAnnouncementMutations.js";
import { useSystemAnnouncementPermissions } from "../../hooks/useSystemAnnouncementPermissions.js";
import { useSystemAnnouncements } from "../../hooks/useSystemAnnouncements.js";
import { computeAnnouncementStats } from "../../utils/systemAnnouncementMapper.js";
import { AnnouncementActionConfirmDialog } from "../AnnouncementActionConfirmDialog.jsx";
import { CreateSystemAnnouncementDrawer } from "../CreateSystemAnnouncementDrawer.jsx";
import { SystemAnnouncementFilterBar } from "../SystemAnnouncementFilterBar.jsx";
import { SystemAnnouncementStatsCards } from "../SystemAnnouncementStatsCards.jsx";
import { SystemAnnouncementTable } from "../SystemAnnouncementTable.jsx";
import { SystemOperationsEmptyState } from "../SystemOperationsEmptyState.jsx";
import { SystemOperationsForbiddenState } from "../SystemOperationsForbiddenState.jsx";

export function SystemAnnouncementsTab({ announcementFilters, onAnnouncementFiltersChange, onNotify }) {
  const permissions = useSystemAnnouncementPermissions();
  const { result, status, errorMessage, refetch } = useSystemAnnouncements({
    announcementFilters,
    enabled: permissions.canViewAnnouncements,
  });
  const [createOpen, setCreateOpen] = useState(false);
  const [actionRequest, setActionRequest] = useState(null);

  const mutations = useSystemAnnouncementMutations({
    onSuccess: () => {
      refetch();
      setActionRequest(null);
      onNotify?.({ variant: "success", message: "Đã cập nhật thông báo." });
    },
    onError: (error) => {
      onNotify?.({ variant: "error", message: error?.message || "Không thể thực hiện thao tác." });
    },
  });

  const stats = useMemo(() => computeAnnouncementStats(result?.items), [result?.items]);
  const currentPage = Number(announcementFilters?.page) || 1;
  const totalPages = result?.totalPages || 0;

  const handleActionRequest = (request) => {
    const { type, item } = request;
    if (type === "publish" && !permissions.canPublishAnnouncements) {
      onNotify?.({ variant: "error", message: "Thiếu quyền SYSTEM_ANNOUNCEMENT_PUBLISH." });
      return;
    }
    if ((type === "pin" || type === "unpin") && !permissions.canUpdateAnnouncements) {
      onNotify?.({ variant: "error", message: "Thiếu quyền SYSTEM_ANNOUNCEMENT_UPDATE." });
      return;
    }
    if (type === "cancel" && !permissions.canCancelAnnouncements) {
      onNotify?.({ variant: "error", message: "Thiếu quyền SYSTEM_ANNOUNCEMENT_CANCEL." });
      return;
    }
    setActionRequest({ type, item });
  };

  const handleConfirmAction = async (request) => {
    const id = request.item.announcementId;
    if (request.type === "publish") await mutations.publishAnnouncement(id);
    if (request.type === "pin") await mutations.pinAnnouncement(id, true);
    if (request.type === "unpin") await mutations.pinAnnouncement(id, false);
    if (request.type === "cancel") await mutations.cancelAnnouncement(id);
  };

  if (!permissions.canViewAnnouncements) {
    return (
      <div className="space-y-6">
        <TabPanelHeader title={SYSTEM_ANNOUNCEMENTS_TITLE} subtitle={SYSTEM_ANNOUNCEMENTS_SUBTITLE} />
        <SystemOperationsForbiddenState message={SYSTEM_ANNOUNCEMENTS_FORBIDDEN} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <TabPanelHeader title={SYSTEM_ANNOUNCEMENTS_TITLE} subtitle={SYSTEM_ANNOUNCEMENTS_SUBTITLE} />

      <SystemAnnouncementStatsCards stats={stats} totalElements={result?.totalElements} />

      <AccountCard>
        <SystemAnnouncementFilterBar
          key={[announcementFilters?.q, announcementFilters?.status, announcementFilters?.severity].join("|")}
          filters={announcementFilters}
          onApply={(next) => onAnnouncementFiltersChange?.(next)}
          onReset={() =>
            onAnnouncementFiltersChange?.({
              q: "",
              status: "",
              severity: "",
              page: "1",
              size: String(ANNOUNCEMENT_PAGE_SIZE),
            })
          }
        />
      </AccountCard>

      {permissions.canCreateAnnouncements ? (
        <div className="flex justify-end">
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
          >
            Tạo thông báo
          </button>
        </div>
      ) : null}

      {status === "loading" ? <AccountSkeleton /> : null}
      {status === "forbidden" ? <SystemOperationsForbiddenState message={errorMessage} /> : null}
      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button type="button" onClick={refetch} className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white">
            {GENERIC_RETRY}
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-on-surface-variant">
              Trang {result?.page}/{Math.max(result?.totalPages, 1)}
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() =>
                  onAnnouncementFiltersChange?.({
                    ...announcementFilters,
                    page: String(currentPage - 1),
                  })
                }
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Trước
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() =>
                  onAnnouncementFiltersChange?.({
                    ...announcementFilters,
                    page: String(currentPage + 1),
                  })
                }
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          </div>
          {result?.items?.length ? (
            <SystemAnnouncementTable items={result.items} onActionRequest={handleActionRequest} />
          ) : (
            <SystemOperationsEmptyState message={SYSTEM_ANNOUNCEMENTS_EMPTY} />
          )}
        </AccountCard>
      ) : null}

      <CreateSystemAnnouncementDrawer
        open={createOpen}
        pending={mutations.pending}
        onClose={() => setCreateOpen(false)}
        onSubmit={async (form) => {
          await mutations.createAnnouncement(form);
          setCreateOpen(false);
        }}
      />

      <AnnouncementActionConfirmDialog
        request={actionRequest}
        pending={mutations.pending}
        onClose={() => setActionRequest(null)}
        onConfirm={handleConfirmAction}
      />
    </div>
  );
}