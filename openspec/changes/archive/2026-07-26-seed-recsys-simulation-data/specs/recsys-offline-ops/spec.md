## ADDED Requirements

### Requirement: Export purchase profile job hook
The offline FastAPI service SHALL expose a job endpoint (or documented CLI equivalent invoked by the same package) to run purchase-profile export from Commerce, accepting optional `as_of` / `T_cut`, returning success with output path summary or structured failure when Commerce configuration is missing.

#### Scenario: Trigger export purchase profile
- **WHEN** an operator calls the export-purchase-profile job with valid `COMMERCE_POSTGRES_URL` (or equivalent) and optional `as_of`
- **THEN** the service runs the export and returns success with the profile path or structured failure detail

#### Scenario: Still no feed predict route
- **WHEN** a client lists offline routes after this change
- **THEN** there is still no public endpoint that returns ranked posts for a user feed request
