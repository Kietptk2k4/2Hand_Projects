## ADDED Requirements

### Requirement: Track sync cooldown is shared via Redis

When the commerce service decides whether to pull GHN order status for a shipment, the system SHALL enforce the configured track-sync cooldown using a Redis key per shipment so that multiple commerce instances share the same cooldown window. Force sync SHALL bypass the cooldown.

#### Scenario: Sync skipped while cooldown key exists

- **WHEN** a non-force track sync is requested for a shipment
- **AND** Redis contains an active `commerce:ghn:track-cooldown:{shipmentId}` key
- **THEN** the system SHALL NOT call the GHN order-info API for that sync attempt

#### Scenario: Sync allowed and cooldown marked after success

- **WHEN** a non-force track sync is allowed (no cooldown key)
- **AND** the GHN pull sync completes successfully enough to mark synced under existing use-case rules
- **THEN** the system sets `commerce:ghn:track-cooldown:{shipmentId}` with TTL equal to the configured `trackSyncCooldownSeconds`

#### Scenario: Force sync bypasses cooldown

- **WHEN** a track sync is requested with force enabled
- **THEN** the system proceeds with the GHN pull regardless of an existing cooldown key

#### Scenario: Redis unavailable allows sync

- **WHEN** Redis is unavailable while evaluating or marking track-sync cooldown
- **THEN** the system treats cooldown as not blocking (allows sync)
- **AND** logs a warning for the Redis failure
