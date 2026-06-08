from pathlib import Path

idx = Path(r"d:/Projects/2Hand_Projects/frontend/src/mocks/handlers/index.js")
text = idx.read_text(encoding="utf-8")
if "adminSystemOperationsHandlers" not in text:
    text = text.replace(
        'import { adminOrderSupportHandlers } from "./adminOrderSupportHandlers";',
        'import { adminOrderSupportHandlers } from "./adminOrderSupportHandlers";\nimport { adminSystemOperationsHandlers } from "./adminSystemOperationsHandlers";',
    )
    text = text.replace(
        "  ...adminOrderSupportHandlers,",
        "  ...adminOrderSupportHandlers,\n  ...adminSystemOperationsHandlers,",
    )
    idx.write_text(text, encoding="utf-8")
    print("index.js patched")

auth = Path(r"d:/Projects/2Hand_Projects/frontend/src/mocks/handlers/adminAuthHandlers.js")
text = auth.read_text(encoding="utf-8")
block = """  "SYSTEM_CONFIG_VIEW",
  "SYSTEM_CONFIG_UPDATE",
  "SYSTEM_ANNOUNCEMENT_CREATE",
  "SYSTEM_ANNOUNCEMENT_UPDATE",
  "SYSTEM_ANNOUNCEMENT_PUBLISH",
  "SYSTEM_ANNOUNCEMENT_CANCEL",
"""
if "SYSTEM_CONFIG_VIEW" not in text:
    text = text.replace('  "ADMIN_AUDIT_VIEW",\n];', '  "ADMIN_AUDIT_VIEW",\n' + block + "];")
    auth.write_text(text, encoding="utf-8")
    print("adminAuthHandlers patched")

tabs = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/admin/adminTabs.js")
text = tabs.read_text(encoding="utf-8")
if "systemOperations" not in text:
    insert = """  {
    id: "systemOperations",
    labelVn: "Vận hành hệ thống",
    labelEn: "System Operations",
  },
"""
    text = text.replace('  {\n    id: "orderSupport",', insert + '  {\n    id: "orderSupport",')
    tabs.write_text(text, encoding="utf-8")
    print("adminTabs patched")