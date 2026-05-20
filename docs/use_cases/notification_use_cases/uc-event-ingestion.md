# UC - Event Ingestion

## 1. Overview

Use case nay mo ta cach Notification Service nhan domain event tu Auth, Social, Commerce va Admin, validate envelope, deduplicate va luu vao `notification_events` voi trang thai `PENDING`.

Muc tieu la bien broker message at-least-once thanh durable internal queue an toan, khong tao duplicate processing khi producer/broker publish lai cung mot event.

## 2. Actors

- **Producer Service:** Auth/Social/Commerce/Admin.
- **Message Broker:** Deliver event at-least-once.
- **Notification Consumer:** Consume va persist event.
- **System Operator:** Monitor ingestion failure.

## 3. Related Data

- `notification_events`
- Producer `outbox_events` logical upstream.

## 4. Preconditions

- Producer event da duoc publish qua outbox/broker.
- Event envelope co `event_id`, `event_type`, `source_service`, `payload`.
- Notification consumer co quyen connect broker va ghi DB.

## 5. Business Rules

- `event_id` cua producer la idempotency key chinh.
- Duplicate `(source_service, source_event_id)` phai duoc xem la success.
- Event khong duoc chua password, token, OTP secret, provider credential.
- Consumer khong goi service producer de mutate nghiep vu.
- Broker message chi duoc ack sau khi DB insert hoac dedup decision thanh cong.

## 6. Sub-Use Cases

### UC-INGEST-01: Ingest Valid Event

Main flow:

1. Broker deliver message cho Notification consumer.
2. Consumer parse JSON envelope.
3. Consumer validate `event_type`, `source_service`, idempotency fields va payload size.
4. Consumer sanitize fields co nguy co logging/persist unsafe.
5. Consumer insert `notification_events` voi `status = PENDING`.
6. Consumer ack broker message.

Postconditions:

- Co mot row `notification_events` moi.
- Row san sang cho processing worker.

### UC-INGEST-02: Handle Duplicate Event

Main flow:

1. Broker redeliver event da tung ingest.
2. Consumer insert vao `notification_events`.
3. DB unique constraint bao duplicate theo `(source_service, source_event_id)` hoac `(source_service, event_key)`.
4. Consumer treat duplicate as success.
5. Consumer ack broker message.

Postconditions:

- Khong co row duplicate.
- Queue khong bi block boi duplicate message.

### UC-INGEST-03: Reject Invalid Event

Main flow:

1. Consumer nhan event invalid.
2. Consumer xac dinh loi: missing event type, invalid source, missing idempotency key, malformed JSON.
3. Consumer log sanitized error.
4. Consumer mark failed/ack/reject theo broker policy.

Exception flow:

- Neu DB unavailable, consumer khong ack de broker retry.

## 7. Failure Cases

- DB unavailable.
- Malformed JSON.
- Missing `event_id` va `event_key`.
- Unsupported `source_service`.
- Payload co sensitive data.

## 8. Security

- Internal consumer credentials required.
- Do not log raw payload if payload may contain token/OTP/secret.
- `source_service` must be allowlisted.

## 9. Acceptance Criteria

- Valid event is stored with `PENDING`.
- Duplicate event does not create duplicate row.
- Invalid event does not crash consumer.
- Broker message is not acknowledged before durable insert/dedup.
- Sensitive data is not logged.

