// calc-order-code.js — node calc-order-code.js <payment-uuid> <order-uuid>
const paymentId = process.argv[2];
const orderId = process.argv[3];

function javaUuidBits(uuid) {
  const p = uuid.split("-");
  let msb = BigInt("0x" + p[0]);
  msb = (msb << 16n) | BigInt("0x" + p[1]);
  msb = (msb << 16n) | BigInt("0x" + p[2]);
  let lsb = BigInt("0x" + p[3]);
  lsb = (lsb << 48n) | BigInt("0x" + p[4]);
  const toSigned = (b) => {
    const n = Number(b);
    return b >= 0x8000000000000000n ? n - 2 ** 64 : n;
  };
  return { msb: toSigned(msb), lsb: toSigned(lsb) };
}

function appOrderCode(paymentUuid) {
  const { msb, lsb } = javaUuidBits(paymentUuid);
  return Math.abs(msb ^ lsb);
}

function appDescription(orderUuid) {
  let suffix = orderUuid.replace(/-/g, "");
  if (suffix.length > 7) suffix = suffix.slice(-7);
  return "DH" + suffix;
}

const orderCode = appOrderCode(paymentId);
const PAYOS_MAX = 9007199254740991;

console.log("paymentId:", paymentId);
console.log("orderId:", orderId);
console.log("orderCode (app):", orderCode);
console.log("exceeds PayOS max?", orderCode > PAYOS_MAX, "(max:", PAYOS_MAX + ")");
console.log("description (app):", appDescription(orderId));