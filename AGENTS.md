# Working on TutorTrack

Read `CONTEXT.md` before changing code. It records the current feature contracts,
increment status and integration risks; verify its status against Git and GitHub.

For every implementation increment, update `CONTEXT.md` in the same PR with the
owner, scope, actual validation results, integration notes and remaining work.
Distinguish local implementation, open PRs and merged work. Do not claim a PR is
merged, tests passed or a feature is available without verifying it. Keep the active
summary concise and retain a short dated history of completed increments.

Follow the course's issue -> feature branch in a member's fork -> reviewed PR ->
merge workflow. Use the appropriate milestone and link the issue and PR in the
context file when available. A context entry does not replace an issue, review,
User Guide or Developer Guide update.

Follow the existing Java style and the course's Java/Git conventions. Add meaningful
tests for changed behavior, run `./gradlew check coverage`, and record failures or
checks that could not run. Credit AI assistance and external reuse according to the
course policy. Preserve teammates' work and coordinate shared model changes.

## Mac and Windows verification

Run focused Java 25 tests on your development machine after code changes. Before
opening a PR or preparing a release, run the full `check coverage` tasks with the
Gradle wrapper on macOS (`./gradlew`) and Windows (`gradlew.bat`), using a teammate's
machine, an isolated remote workspace or cross-platform CI as available.

Verify the exact source revision or snapshot on each platform, use disposable test
data, and record Java version, test totals, failures and skipped checks in
`CONTEXT.md`. Never overwrite another developer's work or transfer credentials or
real application data. If a platform is unavailable, finish other checks and record
the gap. Test-suite success is not a packaged-app GUI smoke test. Personal remote
connection aliases and machine-specific skills belong in local setup instructions.
