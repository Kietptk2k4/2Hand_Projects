import { getAllSellerOrderItemRecords } from "./commerceSellerOrderData";
import { getShopBySellerId } from "./commerceSellerShopData";

const COMMISSION_RATE = 0.1;

const MIN_PAYOUT_AMOUNT = 100_000;
const accountsBySeller = new Map();
const requests = [];

function requireShop(userId) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }
  return { shop };
}

function sellerAccounts(userId) {
  if (!accountsBySeller.has(userId)) {
    accountsBySeller.set(userId, []);
  }
  return accountsBySeller.get(userId);
}

function findAccount(userId, accountId) {
  return sellerAccounts(userId).find((item) => item.id === accountId);
}

function computeLedgerNet(userId) {
  return getAllSellerOrderItemRecords(userId)
    .filter((item) => item.item_status === "COMPLETED" && item.payment?.status === "PAID")
    .reduce((sum, item) => {
      const gross = item.final_price || 0;
      return sum + Math.round(gross * (1 - COMMISSION_RATE));
    }, 0);
}

function computeAvailableBalance(userId) {
  const ledgerNet = computeLedgerNet(userId);
  const pending = getSellerPendingPayoutAmount(userId);
  const debited = getSellerPaidPayoutAmount(userId);
  return Math.max(0, ledgerNet - pending - debited);
}

function enrichRequest(request) {
  const account = findAccount(request.seller_id, request.payout_account_id);
  return {
    ...request,
    bank_name: account?.bank_name ?? "",
    bank_account_name: account?.bank_account_name ?? "",
    bank_account_number: account?.bank_account_number ?? "",
  };
}

export function listSellerPayoutAccounts(userId) {
  const guard = requireShop(userId);
  if (guard.error) return guard;
  return { data: { accounts: sellerAccounts(userId) } };
}

export function createSellerPayoutAccount(userId, body) {
  const guard = requireShop(userId);
  if (guard.error) return guard;
  const accounts = sellerAccounts(userId);
  if (body.is_default) {
    accounts.forEach((item) => {
      item.is_default = false;
    });
  }
  const now = new Date().toISOString();
  const account = {
    id: crypto.randomUUID(),
    bank_name: body.bank_name,
    bank_account_name: body.bank_account_name,
    bank_account_number: body.bank_account_number,
    is_default: Boolean(body.is_default),
    created_at: now,
    updated_at: now,
  };
  accounts.push(account);
  return { data: account };
}

export function listSellerPayoutRequests(userId, { status, page = 1, limit = 20 } = {}) {
  const guard = requireShop(userId);
  if (guard.error) return guard;
  let items = requests.filter((item) => item.seller_id === userId);
  if (status) items = items.filter((item) => item.status === status);
  items = items.sort((a, b) => String(b.requested_at).localeCompare(String(a.requested_at)));
  const total = items.length;
  const start = (Number(page) - 1) * Number(limit);
  const slice = items.slice(start, start + Number(limit)).map(enrichRequest);
  return {
    data: {
      items: slice,
      pagination: {
        page: Number(page),
        limit: Number(limit),
        total_items: total,
        total_pages: total === 0 ? 0 : Math.ceil(total / Number(limit)),
        has_next: start + Number(limit) < total,
      },
    },
  };
}

export function createSellerPayoutRequest(userId, body) {
  const guard = requireShop(userId);
  if (guard.error) return guard;
  const account = findAccount(userId, body.payout_account_id);
  if (!account) {
    return { error: "COMMERCE-404-PAYOUT-ACCOUNT", status: 404, message: "Khong tim thay tai khoan rut tien." };
  }
  const amount = Number(body.amount);
  if (!Number.isFinite(amount) || amount < MIN_PAYOUT_AMOUNT) {
    return { error: "COMMERCE-400-PAYOUT-MIN", status: 400, message: "So tien rut duoi muc toi thieu." };
  }
  if (amount > computeAvailableBalance(userId)) {
    return { error: "COMMERCE-409-PAYOUT-BALANCE", status: 409, message: "So du khong du de rut tien." };
  }
  const now = new Date().toISOString();
  const request = {
    id: crypto.randomUUID(),
    seller_id: userId,
    payout_account_id: body.payout_account_id,
    amount,
    status: "REQUESTED",
    admin_note: null,
    bank_transfer_ref: null,
    requested_at: now,
    approved_at: null,
    paid_at: null,
    rejected_at: null,
    cancelled_at: null,
  };
  requests.unshift(request);
  return { data: enrichRequest(request) };
}

export function cancelSellerPayoutRequest(userId, payoutRequestId) {
  const guard = requireShop(userId);
  if (guard.error) return guard;
  const request = requests.find((item) => item.id === payoutRequestId && item.seller_id === userId);
  if (!request) {
    return { error: "COMMERCE-404-PAYOUT-REQUEST", status: 404, message: "Khong tim thay yeu cau rut tien." };
  }
  if (request.status !== "REQUESTED") {
    return { error: "COMMERCE-409-PAYOUT-STATE", status: 409, message: "Trang thai yeu cau khong hop le." };
  }
  request.status = "CANCELLED";
  request.cancelled_at = new Date().toISOString();
  return { data: enrichRequest(request) };
}

export function listAdminPayoutRequests({ status, page = 1, limit = 20 } = {}) {
  let items = [...requests];
  if (status) items = items.filter((item) => item.status === status);
  items = items.sort((a, b) => String(b.requested_at).localeCompare(String(a.requested_at)));
  const total = items.length;
  const start = (Number(page) - 1) * Number(limit);
  const slice = items.slice(start, start + Number(limit)).map(enrichRequest);
  return {
    data: {
      items: slice,
      pagination: {
        page: Number(page),
        limit: Number(limit),
        total_items: total,
        total_pages: total === 0 ? 0 : Math.ceil(total / Number(limit)),
        has_next: start + Number(limit) < total,
      },
    },
  };
}

export function approveAdminPayoutRequest(payoutRequestId) {
  const request = requests.find((item) => item.id === payoutRequestId);
  if (!request) return { error: "COMMERCE-404-PAYOUT-REQUEST", status: 404, message: "Not found" };
  if (request.status !== "REQUESTED") {
    return { error: "COMMERCE-409-PAYOUT-STATE", status: 409, message: "Invalid state" };
  }
  request.status = "APPROVED";
  request.approved_at = new Date().toISOString();
  return { data: enrichRequest(request) };
}

export function rejectAdminPayoutRequest(payoutRequestId, adminNote = "") {
  const request = requests.find((item) => item.id === payoutRequestId);
  if (!request) return { error: "COMMERCE-404-PAYOUT-REQUEST", status: 404, message: "Not found" };
  if (request.status !== "REQUESTED") {
    return { error: "COMMERCE-409-PAYOUT-STATE", status: 409, message: "Invalid state" };
  }
  request.status = "REJECTED";
  request.admin_note = adminNote;
  request.rejected_at = new Date().toISOString();
  return { data: enrichRequest(request) };
}

export function markAdminPayoutRequestPaid(payoutRequestId, bankTransferRef) {
  const request = requests.find((item) => item.id === payoutRequestId);
  if (!request) return { error: "COMMERCE-404-PAYOUT-REQUEST", status: 404, message: "Not found" };
  if (request.status !== "APPROVED") {
    return { error: "COMMERCE-409-PAYOUT-STATE", status: 409, message: "Invalid state" };
  }
  request.status = "PAID";
  request.bank_transfer_ref = bankTransferRef;
  request.paid_at = new Date().toISOString();
  return { data: enrichRequest(request) };
}

export function getSellerPendingPayoutAmount(userId) {
  return requests
    .filter((item) => item.seller_id === userId && ["REQUESTED", "APPROVED"].includes(item.status))
    .reduce((sum, item) => sum + item.amount, 0);
}

export function getSellerPaidPayoutAmount(userId) {
  return requests
    .filter((item) => item.seller_id === userId && item.status === "PAID")
    .reduce((sum, item) => sum + item.amount, 0);
}
