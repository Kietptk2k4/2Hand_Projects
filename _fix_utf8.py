from pathlib import Path
content = """export const ORDER_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "\u1ea0o g\u1ea7n nh\u1ea5t" },
  { value: "updated_at", label: "C\u1eadp nh\u1eadt g\u1ea7n nh\u1ea5t" },
];

export const ORDER_LIST_STATUS_OPTIONS = [
  { value: "", label: "T\u1ea5t c\u1ea3 tr\u1ea1ng th\u00e1i" },
  { value: "CREATED", label: "CREATED" },
  { value: "AWAITING_PAYMENT", label: "AWAITING_PAYMENT" },
  { value: "PROCESSING", label: "PROCESSING" },
  { value: "COMPLETED", label: "COMPLETED" },
  { value: "CANCELLED", label: "CANCELLED" },
];

export const ORDER_LIST_PAYMENT_METHOD_OPTIONS = [
  { value: "", label: "T\u1ea5t c\u1ea3 ph\u01b0\u01a1ng th\u1ee9c" },
  { value: "COD", label: "COD" },
  { value: "PAYOS", label: "PAYOS" },
];

export const ORDER_LIST_PAGE_SIZE = 20;
"""
target = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/admin/orderSupport/constants/orderListConstants.js")
target.write_text(content, encoding="utf-8")
print(target.read_text(encoding="utf-8").splitlines()[1])
