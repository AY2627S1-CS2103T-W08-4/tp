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
