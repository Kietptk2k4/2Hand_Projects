from pathlib import Path
p = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/admin/adminUrlParams.js")
t = p.read_text(encoding="utf-8")
old = """  } else if (tab === \"system-configs\") {
    for (const key of SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS) {
      const value = next.get(key);
      if (value) next.set(key, value);
    }
  }"""
new = """  } else if (tab === \"system-configs\" && preserve) {
    for (const key of SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }"""
t = t.replace(old, new)
needle = """    if (size) next.set(\"sa_size\", String(size));
  }

  if (clearConfigSelection) {"""
insert = """    if (size) next.set(\"sa_size\", String(size));
  } else if (tab === \"system-announcements\" && preserve) {
    for (const key of SYSTEM_OPERATIONS_ANNOUNCEMENT_FILTER_KEYS) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }

  if (clearConfigSelection) {"""
if "system-announcements\" && preserve" not in t:
    t = t.replace(needle, insert)
t = t.replace(
    "{ tab, configFilters, announcementFilters, configId, configView, clearConfigSelection },",
    "{ tab, configFilters, announcementFilters, configId, configView, clearConfigSelection, preserve },",
)
# pass preserve in build call
t = t.replace(
    "      clearConfigSelection,\n    });",
    "      clearConfigSelection,\n      preserve,\n    });",
)
p.write_text(t, encoding="utf-8")
print("url params fix ok")