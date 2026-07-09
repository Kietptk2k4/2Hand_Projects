import { useMemo, useState } from "react";
import { ANNOUNCEMENT_PAGE_SIZE } from "../../constants/systemAnnouncementConstants.js";
import {
  SYSTEM_ANNOUNCEMENTS_EMPTY,
  SYSTEM_ANNOUNCEMENTS_FORBIDDEN,
  SYSTEM_ANNOUNCEMENTS_SUBTITLE,
  SYSTEM_ANNOUNCEMENTS_TITLE,
} from "../../constants/systemOperationsUiStrings.js";
import { useSystemAnnouncementMutations } from "../../hooks/useSystemAnnouncementMutations.js";
import { useSystemAnnouncementPermissions } from "../../hooks/useSystemAnnouncementPermissions.js";
import { useSystemAnnouncements } from "../../hooks/useSystemAnnouncements.js";
import { computeAnnouncementStats } from "../../utils/systemAnnouncementMapper.js";
import { AnnouncementActionConfirmDialog } from "../AnnouncementActionConfirmDialog.jsx";
import { CreateSystemAnnouncementDrawer } from "../CreateSystemAnnouncementDrawer.jsx";
import { SystemAnnouncementsTabView } from "./SystemAnnouncementsTabView.jsx";

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

  const summary = useMemo(() => {
    if (!result) return "";
    return `${result.totalElements} thông báo · trang ${result.page}/${Math.max(result.totalPages, 1)}`;
  }, [result]);

  const handleActionRequest = (request) => {
    const { type } = request;
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
    setActionRequest({ type, item: request.item });
  };

  const handleConfirmAction = async (request) => {
    const id = request.item.announcementId;
    if (request.type === "publish") await mutations.publishAnnouncement(id);
    if (request.type === "pin") await mutations.pinAnnouncement(id, true);
    if (request.type === "unpin") await mutations.pinAnnouncement(id, false);
    if (request.type === "cancel") await mutations.cancelAnnouncement(id);
  };

  return (
    <SystemAnnouncementsTabView
      title={SYSTEM_ANNOUNCEMENTS_TITLE}
      subtitle={SYSTEM_ANNOUNCEMENTS_SUBTITLE}
      canViewAnnouncements={permissions.canViewAnnouncements}
      forbiddenMessage={SYSTEM_ANNOUNCEMENTS_FORBIDDEN}
      filterKey={[announcementFilters?.q, announcementFilters?.status, announcementFilters?.severity].join("|")}
      announcementFilters={announcementFilters}
      canCreateAnnouncements={permissions.canCreateAnnouncements}
      stats={stats}
      totalElements={result?.totalElements}
      status={status}
      errorMessage={errorMessage}
      summary={summary}
      currentPage={currentPage}
      totalPages={totalPages}
      items={result?.items}
      emptyMessage={SYSTEM_ANNOUNCEMENTS_EMPTY}
      createDrawer={
        <CreateSystemAnnouncementDrawer
          open={createOpen}
          pending={mutations.pending}
          onClose={() => setCreateOpen(false)}
          onSubmit={async (form) => {
            await mutations.createAnnouncement(form);
            setCreateOpen(false);
          }}
        />
      }
      confirmDialog={
        <AnnouncementActionConfirmDialog
          request={actionRequest}
          pending={mutations.pending}
          onClose={() => setActionRequest(null)}
          onConfirm={handleConfirmAction}
        />
      }
      onApplyFilters={(next) => onAnnouncementFiltersChange?.(next)}
      onResetFilters={() =>
        onAnnouncementFiltersChange?.({
          q: "",
          status: "",
          severity: "",
          page: "1",
          size: String(ANNOUNCEMENT_PAGE_SIZE),
        })
      }
      onPageChange={(nextPage) =>
        onAnnouncementFiltersChange?.({
          ...announcementFilters,
          page: String(nextPage),
          size: announcementFilters?.size || String(ANNOUNCEMENT_PAGE_SIZE),
        })
      }
      onCreateClick={() => setCreateOpen(true)}
      onActionRequest={handleActionRequest}
      onRetry={refetch}
    />
  );
}
