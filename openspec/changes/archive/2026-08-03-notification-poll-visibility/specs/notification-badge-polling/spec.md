## ADDED Requirements

### Requirement: Unread notification poll interval while tab is visible

While the user is authenticated and the browser document is visible, the web client SHALL poll the unread notification count at a fixed interval of 15 seconds.

#### Scenario: Visible tab polls every 15 seconds

- **WHEN** an authenticated user has the application tab visible
- **THEN** the client SHALL request unread notification count on a repeating 15-second interval

#### Scenario: Unauthenticated user does not poll

- **WHEN** the user is not authenticated
- **THEN** the client SHALL NOT run the unread notification poll interval

### Requirement: Pause unread poll when tab is hidden

While the browser document is hidden, the web client SHALL NOT run the unread notification poll interval.

#### Scenario: Hidden tab stops polling

- **WHEN** the authenticated user's tab becomes hidden (`document.hidden` is true / `visibilityState` is `hidden`)
- **THEN** the client SHALL clear the unread notification poll interval and SHALL NOT continue periodic unread-count requests until the tab is visible again

### Requirement: Refetch immediately when tab becomes visible

When an authenticated user's tab becomes visible again, the web client SHALL refetch the unread notification count immediately and then restart the 15-second poll interval.

#### Scenario: Resume visible triggers immediate refetch then interval

- **WHEN** the authenticated user's tab transitions from hidden to visible
- **THEN** the client SHALL perform one unread-count refetch immediately
- **AND** the client SHALL start the 15-second poll interval after that resume path

#### Scenario: Visibility resume preserves toast tracking state

- **WHEN** the tab becomes visible again after being hidden
- **THEN** the client SHALL NOT reset baseline unread tracking or seen notification ids solely because of the visibility change

### Requirement: Badge count updates on unread increase

When a successful unread-count poll (including resume refetch) returns a count greater than the previously tracked unread count for the session, the web client SHALL update the notification bell badge to reflect the new unread count.

#### Scenario: Unread count increase updates badge

- **WHEN** a poll or resume refetch returns an unread count greater than the previously tracked count for the session
- **THEN** the notification bell badge SHALL display the updated unread count (or the existing capped label such as `99+` when applicable)

#### Scenario: Zero unread hides badge

- **WHEN** the tracked unread count is zero
- **THEN** the notification bell badge pill SHALL NOT be shown

### Requirement: New-notification toast on unread increase

When a successful unread-count poll (including resume refetch) detects that the unread count increased above the previously tracked count, the web client SHALL show an in-app new-notification toast.

#### Scenario: Unread count increase shows toast

- **WHEN** a poll or resume refetch returns an unread count greater than the previously tracked count for the session
- **THEN** the client SHALL display a new-notification toast using the existing toast message rules

#### Scenario: Badge and toast both update on the same increase

- **WHEN** a poll or resume refetch detects an unread count increase after baseline is established
- **THEN** the client SHALL update the bell badge unread count
- **AND** the client SHALL show the new-notification toast

#### Scenario: First baseline poll does not toast for pre-existing unread

- **WHEN** the client establishes the initial unread baseline after login
- **THEN** the client SHALL set the badge from the current unread count
- **AND** the client SHALL NOT show a new-notification toast solely because of that baseline seed
