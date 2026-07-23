import { useCallback, useEffect, useMemo, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemAnnouncements } from "../api/systemAnnouncementApi.js";
import { ANNOUNCEMENT_PAGE_SIZE } from "../constants/systemAnnouncementConstants.js";
import {
  ANNOUNCEMENT_LIST_PAGE_SIZE,
  ANNOUNCEMENT_VIEW_MODES,
} from "../constants/announcementListConstants.js";
import { useSystemAnnouncementDetail } from "../hooks/useSystemAnnouncementDetail.js";
import { useSystemAnnouncementMutations } from "../hooks/useSystemAnnouncementMutations.js";
import { useSystemAnnouncementPermissions } from "../hooks/useSystemAnnouncementPermissions.js";
import { useSystemAnnouncementStats } from "../hooks/useSystemAnnouncementStats.js";
import { mapSystemAnnouncementsResponse } from "../utils/systemAnnouncementMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";
import {
  buildAnnouncementQuickFilter,
  removeAnnouncementFilterChip,
} from "../utils/announcementFilterHelpers.js";
import { CreateSystemAnnouncementDrawer } from "./CreateSystemAnnouncementDrawer.jsx";
import { SystemAnnouncementDrawer } from "./SystemAnnouncementDrawer.jsx";
import { SystemAnnouncementListView } from "./SystemAnnouncementListView.jsx";

function buildQueryParams(filters) {
  const params = {
    page: Number(filters?.page) || 1,
    size: Number(filters?.size) || ANNOUNCEMENT_PAGE_SIZE,
  };
  if (filters?.q) params.q = filters.q;
  if (filters?.status) params.status = filters.status;
  if (filters?.severity) params.severity = filters.severity;
  return params;
}

export function SystemAnnouncementListPanel({
  announcementFilters,
  onFiltersChange,
  announcementId,
  announcementView,
  onAnnouncementSelectionChange,
}) {
  const { showSessionExpired } = useAuthSession();
  const permissions = useSystemAnnouncementPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  const filterQ = announcementFilters?.q || "";
  const filterStatus = announcementFilters?.status || "";
  const filterSeverity = announcementFilters?.severity || "";
  const filterPage = Number(announcementFilters?.page) || 1;
  const filterSize = Number(announcementFilters?.size) || ANNOUNCEMENT_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: filterQ,
    status: filterStatus,
    severity: filterSeverity,
  });

  const { stats, status: statsStatus, refetch: refetchStats } = useSystemAnnouncementStats({
    enabled: permissions.canViewAnnouncements,
  });

  const {
    detail: fetchedAnnouncement,
    status: detailStatus,
    refetch: refetchDetail,
  } = useSystemAnnouncementDetail(announcementId);

  const fetchList = useCallback(async () => {
    if (!permissions.canViewAnnouncements) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSystemAnnouncements(buildQueryParams(announcementFilters));
      setResult(mapSystemAnnouncementsResponse(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionHint: "SYSTEM_ANNOUNCEMENT_CREATE",
      });
      setResult(null);
    }
  }, [announcementFilters, permissions.canViewAnnouncements, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    refetchDetail();
  }, [fetchList, refetchDetail, refetchStats]);

  const mutations = useSystemAnnouncementMutations({
    onSuccess: () => {
      refreshAll();
      setToastMessage("Đã cập nhật thông báo.");
    },
    onError: (error) => {
      setToastMessage(error?.message || "Không thể thực hiện thao tác.");
    },
  });

  useEffect(() => {
    setDraftFilters({
      q: filterQ,
      status: filterStatus,
      severity: filterSeverity,
    });
  }, [filterQ, filterSeverity, filterStatus]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...announcementFilters,
        ...patch,
      });
    },
    [announcementFilters, onFiltersChange],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      q: "",
      status: "",
      severity: "",
      page: "1",
      size: String(ANNOUNCEMENT_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildAnnouncementQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      status: next.status,
      severity: next.severity,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeAnnouncementFilterChip(announcementFilters, chipKey);
    setDraftFilters({
      q: next.q,
      status: next.status,
      severity: next.severity,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const listAnnouncement = useMemo(() => {
    if (!announcementId || !result?.items) return null;
    return result.items.find((item) => item.announcementId === announcementId) || null;
  }, [announcementId, result?.items]);

  const selectedAnnouncement = useMemo(() => {
    if (fetchedAnnouncement) return fetchedAnnouncement;
    return listAnnouncement;
  }, [fetchedAnnouncement, listAnnouncement]);

  const currentPage = filterPage || result?.page || 1;
  const totalPages = result?.totalPages || 1;
  const items = result?.items || [];

  const handleRowSelect = (item) => {
    if (!item?.announcementId) return;
    if (item.announcementId === announcementId) {
      onAnnouncementSelectionChange?.({ announcementId: null, announcementView: null });
      return;
    }
    onAnnouncementSelectionChange?.({
      announcementId: item.announcementId,
      announcementView: ANNOUNCEMENT_VIEW_MODES.DETAIL,
    });
  };

  return (
    <>
      <SystemAnnouncementListView
        canViewAnnouncements={permissions.canViewAnnouncements}
        canCreateAnnouncements={permissions.canCreateAnnouncements}
        status={status}
        errorMessage={errorMessage}
        appliedFilters={announcementFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetry={fetchList}
        stats={stats}
        statsStatus={statsStatus}
        onStatPresetClick={handleQuickFilter}
        items={items}
        result={result}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        selectedAnnouncementId={announcementId}
        onRowSelect={handleRowSelect}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: String(nextSize),
          })
        }
        onCreateClick={() => setCreateOpen(true)}
        createDrawer={
          <CreateSystemAnnouncementDrawer
            open={createOpen}
            pending={mutations.pending}
            onClose={() => setCreateOpen(false)}
            onSubmit={async (form) => {
              await mutations.createAnnouncement(form);
              setCreateOpen(false);
              refreshAll();
            }}
          />
        }
        drawer={
          announcementId ? (
            <SystemAnnouncementDrawer
              announcement={selectedAnnouncement}
              loading={detailStatus === "loading" && !selectedAnnouncement}
              announcementView={announcementView || ANNOUNCEMENT_VIEW_MODES.DETAIL}
              canCreate={permissions.canCreateAnnouncements}
              canUpdate={permissions.canUpdateAnnouncements}
              canPublish={permissions.canPublishAnnouncements}
              canCancel={permissions.canCancelAnnouncements}
              pending={mutations.pending}
              onClose={() =>
                onAnnouncementSelectionChange?.({ announcementId: null, announcementView: null })
              }
              onViewChange={(nextView) =>
                onAnnouncementSelectionChange?.({ announcementId, announcementView: nextView })
              }
              onSave={async (form) => {
                if (!announcementId) return;
                await mutations.updateAnnouncement(announcementId, form);
              }}
              onPublish={async (payload) => {
                if (!announcementId) return;
                await mutations.publishAnnouncement(announcementId, payload);
              }}
              onPin={async () => {
                if (!announcementId) return;
                await mutations.pinAnnouncement(announcementId, true);
              }}
              onUnpin={async () => {
                if (!announcementId) return;
                await mutations.pinAnnouncement(announcementId, false);
              }}
              onCancelAnnouncement={async () => {
                if (!announcementId) return;
                await mutations.cancelAnnouncement(announcementId);
              }}
            />
          ) : null
        }
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </>
  );
}
