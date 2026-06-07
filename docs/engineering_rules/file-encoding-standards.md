# File Encoding Standards — 2Hands

**Version:** 1.0  
**Status:** Active  
**Applies to:** All services (`Services/*`), frontend (`frontend/`), docs, Cursor rules

---

## 1. Problem

On **Windows**, automated file creation (Cursor Agent `Write` tool, some editors) can save text as **UTF-16 LE** instead of UTF-8. Symptoms recur across the project:

| Layer | Symptom |
|-------|---------|
| Java / Gradle | `error: illegal character: '\u0000'`, compile fails |
| JavaScript / Vite | `Uncaught SyntaxError: Invalid or unexpected token` at line 1, column ~72 |
| Read/search tools | `File seems to be binary and cannot be opened as text` |
| Git diff | Every character appears spaced or file looks "double width" |

Root cause: UTF-16 stores each ASCII character as two bytes (e.g. `p` → `70 00`), which JVM and JS parsers reject.

---

## 2. Standard

- **Default encoding for all source and docs:** UTF-8 **without BOM**
- **Forbidden for source code:** UTF-16 LE/BE (unless an external tool explicitly requires it)
- **Gradle:** already uses `project.build.sourceEncoding = UTF-8` in services — files on disk must match

---

## 3. Rules for humans and AI agents

1. After creating or bulk-rewriting files, **verify encoding** if the service fails to compile or the browser shows a syntax error on line 1.
2. Prefer incremental `StrReplace` over full-file `Write` when possible.
3. For new files on Windows, safe options:
   - PowerShell: `[System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false))`
   - Python: `Path.write_text(content, encoding="utf-8")`
4. Do **not** commit UTF-16 corrupted files; fix before push.

---

## 4. Detection

### 4.1 First bytes (quick)

```powershell
python -c "print(open(r'Services\social-service\src\main\java\Example.java', 'rb').read(24))"
```

| Result | Meaning |
|--------|---------|
| `b'package com.twohands...'` | UTF-8 OK |
| `b'\xff\xfe...'` or `b'p\x00a\x00c\x00...'` | UTF-16 — fix required |

### 4.2 Scan a folder

```powershell
python -c "
from pathlib import Path
root = Path(r'd:/Projects/2Hand_Projects/Services/social-service/src')
for p in root.rglob('*.java'):
    b = p.read_bytes()[:4]
    if len(b) >= 2 and b[1] == 0 and b[0] < 128:
        print('UTF-16?', p)
"
```

---

## 5. Repair

### 5.1 Single file (UTF-16 → UTF-8)

**PowerShell** (file was UTF-16 LE):

```powershell
$path = "d:\Projects\2Hand_Projects\frontend\src\fe-module\features\commerce\constants\sellerProductBrands.js"
$content = Get-Content -Path $path -Raw -Encoding Unicode
[System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false))
```

**Python:**

```python
from pathlib import Path
p = Path(r"d:/Projects/2Hand_Projects/path/to/file.js")
text = p.read_text(encoding="utf-16")
p.write_text(text, encoding="utf-8")
```

### 5.2 After repair

- Re-run `./gradlew compileJava` or refresh Vite (`Ctrl+Shift+R`)
- Confirm first bytes are normal ASCII/UTF-8

---

## 6. Cursor integration

Workspace rule (always on for agents):

- `.cursor/rules/file-encoding-utf8.mdc`

Agents must read and follow that rule when writing files in this repository.

---

## 7. Related

- `docs/engineering_rules/backend-convention.md` — section File encoding
- `docs/engineering_rules/frontend-convention.md` — section File encoding