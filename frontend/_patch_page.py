from pathlib import Path

nav = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/admin/components/AdminNestedNav.jsx")
text = nav.read_text(encoding="utf-8")
if "SYSTEM_OPERATIONS_TABS" not in text:
    text = text.replace(
        'import { ORDER_SUPPORT_TABS } from "../orderSupport/orderSupportTabs.js";',
        'import { ORDER_SUPPORT_TABS } from "../orderSupport/orderSupportTabs.js";\nimport { SYSTEM_OPERATIONS_TABS } from "../systemOperations/systemOperationsTabs.js";',
    )
    text = text.replace(
        "  orderSupport: ORDER_SUPPORT_TABS,\n};",
        "  orderSupport: ORDER_SUPPORT_TABS,\n  systemOperations: SYSTEM_OPERATIONS_TABS,\n};",
    )

parent_icon = """
  if (sectionId === \"systemOperations\") {
    return (
      <svg className={className} fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\" viewBox=\"0 0 24 24\" aria-hidden=\"true\">
        <path
          d=\"M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z\"
          strokeLinecap=\"round\"
          strokeLinejoin=\"round\"
        />
        <path d=\"M15 12a3 3 0 11-6 0 3 3 0 016 0z\" strokeLinecap=\"round\" strokeLinejoin=\"round\" />
      </svg>
    );
  }
"""
if 'sectionId === "systemOperations"' not in text:
    text = text.replace(
        '  if (sectionId === "orderSupport") {',
        parent_icon + '\n  if (sectionId === "orderSupport") {',
    )

child_icons = """
    settings: (
      <path
        d=\"M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z\"
        strokeLinecap=\"round\"
        strokeLinejoin=\"round\"
      />
    ),
    announcement: (
      <path
        d=\"M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z\"
        strokeLinecap=\"round\"
        strokeLinejoin=\"round\"
      />
    ),
"""
if "announcement:" not in text:
    text = text.replace("    comment: (", child_icons + "    comment: (")

nav.write_text(text, encoding="utf-8")
print("AdminNestedNav patched")

page = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/pages/AdminPage.jsx")
text = page.read_text(encoding="utf-8")

if "parseSystemOperationsConfigFilters" not in text:
    text = text.replace(
        "  parseOrderSupportWebhookFilters,\n} from \"../admin/adminUrlParams.js\";",
        "  parseOrderSupportWebhookFilters,\n  parseSystemOperationsAnnouncementFilters,\n  parseSystemOperationsConfigFilters,\n  parseSystemOperationsConfigId,\n  parseSystemOperationsConfigView,\n} from \"../admin/adminUrlParams.js\";",
    )

if "SystemConfigsTab" not in text:
    text = text.replace(
        'import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";',
        'import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";\nimport { SystemConfigsTab } from "../admin/systemOperations/components/tabs/SystemConfigsTab.jsx";\nimport { SystemAnnouncementsTab } from "../admin/systemOperations/components/tabs/SystemAnnouncementsTab.jsx";',
    )

if "SYSTEM_OPERATIONS_TAB_COMPONENTS" not in text:
    text = text.replace(
        "const ORDER_SUPPORT_TAB_COMPONENTS = {",
        """const SYSTEM_OPERATIONS_TAB_COMPONENTS = {
  \"system-configs\": SystemConfigsTab,
  \"system-announcements\": SystemAnnouncementsTab,
};

const ORDER_SUPPORT_TAB_COMPONENTS = {""",
    )

if "systemOperationsConfigFilters" not in text:
    text = text.replace(
        "  const contentModerationProductView = parseContentModerationProductView(searchParams);",
        """  const contentModerationProductView = parseContentModerationProductView(searchParams);
  const systemOperationsConfigFilters = parseSystemOperationsConfigFilters(searchParams);
  const systemOperationsAnnouncementFilters = parseSystemOperationsAnnouncementFilters(searchParams);
  const systemOperationsConfigId = parseSystemOperationsConfigId(searchParams);
  const systemOperationsConfigView = parseSystemOperationsConfigView(searchParams);""",
    )

handlers = """
  const handleSystemOperationsConfigFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: \"systemOperations\",
          tab: activeChildTab,
          configFilters: filters,
          clearConfigSelection: true,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const handleSystemOperationsAnnouncementFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: \"systemOperations\",
          tab: activeChildTab,
          announcementFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const handleSystemOperationsConfigSelectionChange = useCallback(
    ({ configId: nextConfigId, configView: nextConfigView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: \"systemOperations\",
          tab: activeChildTab,
          configFilters: systemOperationsConfigFilters,
          announcementFilters: systemOperationsAnnouncementFilters,
          configId: nextConfigId || undefined,
          configView: nextConfigView || undefined,
          clearConfigSelection: !nextConfigId,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      searchParams,
      setSearchParams,
      systemOperationsAnnouncementFilters,
      systemOperationsConfigFilters,
    ],
  );
"""

if "handleSystemOperationsConfigFiltersChange" not in text:
    text = text.replace(
        "  const onViewRolePermissions = useCallback(",
        handlers + "\n  const onViewRolePermissions = useCallback(",
    )

if "SystemOperationsTabComponent" not in text:
    text = text.replace(
        "  const OrderSupportTabComponent =",
        "  const SystemOperationsTabComponent =\n    SYSTEM_OPERATIONS_TAB_COMPONENTS[activeChildTab] || SystemConfigsTab;\n  const OrderSupportTabComponent =",
    )

props = """
  const systemOperationsTabProps = {
    configId: systemOperationsConfigId,
    configView: systemOperationsConfigView,
    configFilters: systemOperationsConfigFilters,
    announcementFilters: systemOperationsAnnouncementFilters,
    onFiltersChange: handleSystemOperationsConfigFiltersChange,
    onAnnouncementFiltersChange: handleSystemOperationsAnnouncementFiltersChange,
    onConfigSelectionChange: handleSystemOperationsConfigSelectionChange,
    onNotify,
  };
"""
if "systemOperationsTabProps" not in text:
    text = text.replace(
        "  const contentModerationTabProps = {",
        props + "\n  const contentModerationTabProps = {",
    )

# Fix SystemAnnouncementsTab to use onAnnouncementFiltersChange - I used onFiltersChange in tab but props say onAnnouncementFiltersChange. Update tab file or props.

# Use onAnnouncementFiltersChange in announcements tab - patch tab to accept both or rename prop in AdminPage to match tab

# SystemAnnouncementsTab uses onFiltersChange - systemOperationsTabProps should pass onFiltersChange for announcements tab only. The SystemConfigsTab uses onFiltersChange. So we need different prop names per tab - SystemConfigsTab gets onFiltersChange, SystemAnnouncementsTab gets onFiltersChange too but AdminPage passes same handler wrong for announcements.

# Fix AdminPage props:
text = text.replace(
    """  const systemOperationsTabProps = {
    configId: systemOperationsConfigId,
    configView: systemOperationsConfigView,
    configFilters: systemOperationsConfigFilters,
    announcementFilters: systemOperationsAnnouncementFilters,
    onFiltersChange: handleSystemOperationsConfigFiltersChange,
    onAnnouncementFiltersChange: handleSystemOperationsAnnouncementFiltersChange,
    onConfigSelectionChange: handleSystemOperationsConfigSelectionChange,
    onNotify,
  };""",
    """  const systemOperationsTabProps = {
    configId: systemOperationsConfigId,
    configView: systemOperationsConfigView,
    configFilters: systemOperationsConfigFilters,
    announcementFilters: systemOperationsAnnouncementFilters,
    onFiltersChange: handleSystemOperationsConfigFiltersChange,
    onAnnouncementFiltersChange: handleSystemOperationsAnnouncementFiltersChange,
    onConfigSelectionChange: handleSystemOperationsConfigSelectionChange,
    onNotify,
  };""",
)

if 'adminTopTab === "systemOperations"' not in text:
    text = text.replace(
        '    if (adminTopTab === "orderSupport") {',
        '    if (adminTopTab === "systemOperations") {\n      return <SystemOperationsTabComponent {...systemOperationsTabProps} />;\n    }\n    if (adminTopTab === "orderSupport") {',
    )

# update handleSectionChange and handleChildTabChange deps - add system ops params
for old, new in [
    (
        "          productView:\n            sectionId === \"contentModeration\" ? contentModerationProductView : undefined,\n        }),",
        "          productView:\n            sectionId === \"contentModeration\" ? contentModerationProductView : undefined,\n          configFilters:\n            sectionId === \"systemOperations\" ? systemOperationsConfigFilters : undefined,\n          announcementFilters:\n            sectionId === \"systemOperations\" ? systemOperationsAnnouncementFilters : undefined,\n          configId:\n            sectionId === \"systemOperations\" ? systemOperationsConfigId || undefined : undefined,\n          configView:\n            sectionId === \"systemOperations\" ? systemOperationsConfigView : undefined,\n        }),",
    ),
    (
        "      orderSupportWebhookFilters,\n      setSearchParams,\n    ],\n  );\n\n  const handleChildTabChange",
        "      orderSupportWebhookFilters,\n      systemOperationsConfigFilters,\n      systemOperationsAnnouncementFilters,\n      systemOperationsConfigId,\n      systemOperationsConfigView,\n      setSearchParams,\n    ],\n  );\n\n  const handleChildTabChange",
    ),
    (
        "            adminTopTab === \"contentModeration\" ? contentModerationProductView : undefined,\n          preserve: searchParams,",
        "            adminTopTab === \"contentModeration\" ? contentModerationProductView : undefined,\n          configFilters:\n            adminTopTab === \"systemOperations\" ? systemOperationsConfigFilters : undefined,\n          announcementFilters:\n            adminTopTab === \"systemOperations\" ? systemOperationsAnnouncementFilters : undefined,\n          configId:\n            adminTopTab === \"systemOperations\" ? systemOperationsConfigId || undefined : undefined,\n          configView:\n            adminTopTab === \"systemOperations\" ? systemOperationsConfigView : undefined,\n          clearConfigSelection: adminTopTab === \"systemOperations\" && activeChildTab === \"system-announcements\",\n          preserve: searchParams,",
    ),
    (
        "      orderSupportWebhookFilters,\n      searchParams,\n      setSearchParams,\n    ],\n  );\n\n  const handleInvestigationTargetChange",
        "      orderSupportWebhookFilters,\n      systemOperationsConfigFilters,\n      systemOperationsAnnouncementFilters,\n      systemOperationsConfigId,\n      systemOperationsConfigView,\n      searchParams,\n      setSearchParams,\n    ],\n  );\n\n  const handleInvestigationTargetChange",
    ),
]:
    if new.split("\n")[1].strip() not in text:
        text = text.replace(old, new)

# useMemo deps
if "systemOperationsTabProps" in text and "SystemOperationsTabComponent," not in text:
    text = text.replace(
        "    OrderSupportTabComponent,\n    RoleTabComponent,",
        "    OrderSupportTabComponent,\n    SystemOperationsTabComponent,\n    RoleTabComponent,",
    )
    text = text.replace(
        "    orderSupportTabProps,\n    roleTabProps,",
        "    orderSupportTabProps,\n    systemOperationsTabProps,\n    roleTabProps,",
    )

page.write_text(text, encoding="utf-8")
print("AdminPage patched")