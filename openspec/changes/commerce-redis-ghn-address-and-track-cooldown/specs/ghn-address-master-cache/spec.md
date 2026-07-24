## ADDED Requirements

### Requirement: GHN address master-data is served from Redis when available

When the commerce service lists GHN provinces, districts for a province, or wards for a district, the system SHALL attempt to read a cached list from Redis before calling GHN. On cache miss or Redis failure, the system SHALL call GHN, and on successful GHN response SHALL store the list in Redis with a long TTL.

#### Scenario: Province list cache hit

- **WHEN** a client requests GHN provinces and Redis contains `commerce:ghn:provinces`
- **THEN** the system returns the cached province list
- **AND** does not call the GHN province master-data API

#### Scenario: District list cache miss then populate

- **WHEN** a client requests GHN districts for a province id and Redis has no matching districts key
- **AND** GHN returns a successful district list
- **THEN** the system returns that list
- **AND** stores it under `commerce:ghn:districts:{provinceId}` with TTL of at least 24 hours

#### Scenario: Ward list cache miss then populate

- **WHEN** a client requests GHN wards for a district id and Redis has no matching wards key
- **AND** GHN returns a successful ward list
- **THEN** the system returns that list
- **AND** stores it under `commerce:ghn:wards:{districtId}` with TTL of at least 24 hours

#### Scenario: Redis unavailable falls back to GHN

- **WHEN** Redis is unavailable while listing GHN address master-data
- **THEN** the system calls GHN as if there was no cache
- **AND** still returns a successful response when GHN succeeds
- **AND** logs a warning for the Redis failure
