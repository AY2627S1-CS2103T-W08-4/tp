# TutorTrack team implementation context

Last verified: 2026-10-20 (Singapore time).

## Active release completion

- Owner: Vincent Peh (`Eskalade`); fork branch `codex/v1.3-mvp-release`.
- Issue: [#55](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/55), v1.3.
- Base: team master `0a0944cb4701709fbd5b258f544a7b132f460647`.
  GitHub confirms #44, #51 and #53 merged. Earlier entries below are historical.
- PR: [#56](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/56), open and mergeable.
- Status: implemented and locally validated; not merged or released. The v1.3 milestone
  was already closed at audit, then reopened for issue #55 and PR #56; no tag or release existed.
  Release deadline
  was 15 October 23:59 SGT; this completion is after that deadline.
- Scope: exact class/week query, headings, badges and empty states; selection/context
  handling; strict batch deletion and save-before-live-update; strict Exit format;
  readable detail and empty-roster text; v1.3 version/title; separate Apple Silicon JAR; UG/DG/README.
- Validation: baseline passed 330 tests. The completion build passed 335 tests with
  zero failures/errors/skips, Checkstyle, coverage and both JAR tasks on the final code.
  Repository scripts and `git diff --check` passed.
  Packaged Apple Silicon JAR passed an instrumented real-window MVP workflow and a
  second-JVM reload, using isolated synthetic data. Actual scene screenshots are in
  `docs/images/release-v1.3/`. The standard JAR passed the same workflow and second-JVM reload on Zulu Java
  25.0.3 FX (ARM64); Apple Silicon used plain Temurin Java 25.0.4 (ARM64).
- Failures resolved during development: obsolete AB3 name-query expectations;
  import/style violations; duplicate Index objects bypassed hash-based deduplication
  because Index has no hashCode (now deduplicated by integer value).
- Integration: preserves existing Person/storage contracts. Queries never write;
  deletion retains the live predicate. Repeated batch indices delete each target once
  in first-appearance order (the supplied specification does not define duplicates).
  `find` now follows the required class/week grammar; name search is outside the MVP.
- The user explicitly authorized review and merge without teammate review for this
  release. Do not describe this exception as teammate approval. Implementation/test
  assistance: OpenAI Codex; existing teammate work and SE-EDU reuse are retained.
- Review: checked parsing/error precedence, displayed-index snapshots, save-before-live-update,
  view transitions, attendance isolation, native packaging and documentation. The repeated-index
  defect found during validation is fixed; no known blocking findings remain.
- Remote CI: fork Java CI [run 39385173412](https://github.com/Eskalade/tp/actions/runs/39385173412)
  passed on Windows, macOS and Linux for implementation commit `9aa8dfaf`.
  Final-head verification remains required; no team check runs existed at the last check.
- Remaining: GitHub CI verification, merge, build exact
  merged revision, publish JARs/checksums/notes with screenshots, verify downloads.
  Actual Windows/Linux launches are not yet verified locally. The standard JAR uses
  Intel Mac natives; plain ARM Java must use the separate Apple Silicon JAR.
- 2026-10-20: audited the agreed MVP document, found missing class query and complete
  deletion behavior despite the closed milestone; implemented release completion.

## Historical handoffs (superseded by the active summary above)


Last verified: 2026-10-15 (Singapore time). This is a handoff, not a substitute for
live GitHub status, issues, teammate review or UG/DG updates.

## Project and workflow

- Team: https://github.com/AY2627S1-CS2103T-W08-4/tp
- TutorTrack: Java 25 / JavaFX / Gradle / JSON desktop student roster.
- v1.2 is closed on GitHub. v1.3 is the first working MVP release, due
  **2026-10-15 23:59 SGT**; the GitHub milestone currently has a later due date.
- Week 9: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/project.html
- Use assigned issues -> feature branch in member fork -> reviewed PR -> team merge.
  Release JAR, release notes with screenshots/recordings and milestone wrap-up are team tasks.

## Feature ownership and verified integration status

| Feature | Owner | Team master / published status |
| --- | --- | --- |
| Add student | Vincent Peh (`Eskalade`) | Telegram type merged #28; v1.3 integration in current increment below |
| Delete student | Ethan (`eatenquek`), based on authored PR | #40 open, unmerged; current diff changes terminology/docs, not full batch deletion |
| List students / view class | Jian Yang (`jianyang999`) | ClassMembership merged #34; class/week find integration not on master |
| Record/correct attendance | Min Thi Ha (`toomintyy`) | AttendanceStatus merged #36; mark/persistence integration not on master |
| Exit | Aston (`aston-ish`) | Argument validation merged #39; full integrated shutdown verification remains |
| General save/reload | Not confirmed | Add-specific transaction below; generalized rollback ownership needs coordination |

Verified team master: `b3e1cf4de2188a23d175b92cd1dd9b9167120275` (2026-10-12).
Its Java CI passed: https://github.com/AY2627S1-CS2103T-W08-4/tp/actions/runs/38028391769
Unpublished teammate work is unknown. The open tutorial PR #26 is not product functionality.

## Merge repair (2026-10-15)

- Owner: Vincent (`Eskalade`), with Codex assistance; issue #43 / PR #44.
- Synced the user's conflict resolution at `88b1a798` with team master `d4e26e25`.
  #40, #47 and #49 are now merged. #44/#51/#53 are still open.
- Fixed the integration between strict add parsing and the merged list parser:
  identify the command separately, then pass original argument text to strict parsers.
  This restores the specific list-format error for leading tabs without accepting
  control whitespace in add. Added a leading-control-whitespace add regression test.
- Validation: `./gradlew check coverage` passed, 299 tests, no failures/errors/skips.
  The initial run reproduced one list-format regression; the fix resolves it.
- GitHub reports mergeability unknown and no checks for the latest pre-repair head;
  Actions is enabled. The pushed repair must receive fresh CI/review; no merge claimed.

### Attendance merge repair (2026-10-15)

- Vincent (`Eskalade`), with Codex assistance, integrated the latest #44 repair
  into #51 at the user's request. Preserved attendance implementation and both
  handoffs; strict mark parsing now receives raw arguments like add/list.
- Validation: `./gradlew check coverage` passed: 319 tests, zero failures/errors/skips,
  Checkstyle and coverage passed. Added main-parser control-whitespace regression coverage.
- Merge order remains #44 before #51. No upstream merge performed; GitHub CI is unverified.

## Combined exit merge repair (2026-10-15)

- Vincent (`Eskalade`), with Codex assistance, resolved #53's conflicts with
  current master and integrated repaired #44/#51 without discarding feature code.
  Both ExitCommand and MarkCommand imports/transaction paths are preserved.
- Validation: `./gradlew check coverage shadowJar` passed locally on Java 25.0.4:
  330 tests, zero failures/errors/skips, including real-window exit tests;
  Checkstyle, coverage and JAR build passed. `git diff --check` passed.
- Merge order: #44 -> #51 -> #53. These PRs remain open; no upstream merge is claimed.
  GitHub confirmed #51 mergeable/clean after the repair; final-head CI still needs verification.

## Exit integration

### Exit handling and verification (2026-10-15)

- Owner: Aston (`aston-ish`); branch: `exit-handling-verification`; milestone: v1.3.
- Base: team master `b3e1cf4d`, fetched and verified on GitHub on 2026-10-15.
- Issue: [#52](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/52).
- Status: published in PR #53, not merged. Latest integration includes #44 and #51; merge those first.
- Scope: route typed/menu/window-close Exit through saving; block closing on save
  failure; show an actionable wrapped error; preserve both windows until save
  succeeds; retain argument validation. Add 11 logic/real-window GUI tests, UG/DG
  updates and accurate AI credit. Linux GUI CI uses Xvfb; Gradle dependencies are unchanged.
- Branch validation: `./gradlew.bat check coverage shadowJar` passed on Temurin
  Java 25.0.4 on Windows: 276 tests, zero failures/errors/skips, both Checkstyle
  tasks and coverage passed. Tests cover IO/permission errors, failure/retry,
  state/file preservation, all three close routes, open Help and legacy reload.
- Remote branch CI: implementation commit `d8ee0a0e` passed Windows, macOS and
  Linux builds, including the real-window tests and Linux repository scripts:
  [run 38732753618](https://github.com/aston-ish/tp/actions/runs/38732753618).
  Repository scripts and `git diff --check` also passed locally. Recheck the latest
  PR checks after opening the PR or making further changes.
- Combined local verification: isolated checkout of open PR #51 at `c7e88a57`
  (includes PR #44 `8832ae33` and PR #49 `a135c683`), with this Exit patch and two
  additional integration tests, passed 323 tests with zero failures/errors/skips,
  Checkstyle, coverage and JAR build. A three-way patch application required only
  combining the Exit and Mark imports; both feature paths were preserved.
- Packaged-app smoke: an instrumented app launched in two separate JVMs using
  isolated temporary data. It added two complete students, recorded/corrected/
  cleared attendance, rejected invalid Exit, kept both windows open on injected
  File > Exit failure, retried typed Exit, reloaded all data, and closed via the
  window button. Both JVMs reached `MainApp.stop()` and exited normally; preferences
  were saved. Student/course/week isolation and cleared-record removal were verified.
- Artifacts: `build/exit-verification/` holds the isolated test source, process
  probe/logs and screenshots. Exit failure feedback now wraps completely. Snapshot
  inspection found light-on-light attendance detail text inherited from PR #51;
  its owner needs to fix/verify that rendering before an integrated GUI release.
- Integration: PRs #44/#49/#51 are still open at the recorded heads. No dependency
  is merged into this feature branch. General storage/model changes remain with
  their owners; the existing tutorial branch is retained separately.
- Remaining: create the v1.3 PR, obtain review and CI on the latest commit, combine
  reviewed dependencies in merge order, repeat acceptance checks on final team
  master, complete independent manual/cross-platform GUI checks, and release as a team.

### Completed Exit argument validation

- Issue #37 and [PR #39](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/39)
  merged into team master on 2026-10-09, verified through GitHub on 2026-10-15.
  PR #38 was closed without merging after its source branch was renamed.
- Original local validation: 252 tests, zero failures/errors/skips; the parser
  reached 100% line/branch coverage. V1.3 lifecycle validation is recorded above.
## Current increment: v1.3 attendance

- Owner: `toomintyy`; branch: `attendance-v13`; issue
  [#50](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/50), milestone v1.3.
- PR: [#51](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/51), published for review; not merged.
- This branch includes the published
  #44 student implementation (`8832ae33`) and #49 week type (`a135c683`). Merge
  those dependencies first; the local merge does not merge either upstream PR.
- Scope: mark parser/command, immutable per-student/course/week records, JSON
  validation and persistence, save-before-live-update transaction, detail rendering
  and an optional roster badge interface. No-op commands return selection without saving.
- Roster handoff: #45 should call `PersonListPanel.setAttendanceContext(membership,
  week)` after a successful query, and `(null, null)` after list/name search. Changing
  week refreshes cards even if the filtered list is unchanged. Class queries/headings
  remain Jian Yang's work; they are not enabled by this branch.
- Validation: `./gradlew check coverage shadowJar` passed on Zulu Java 25.0.3:
  310 tests, zero failures/errors/skips; both Checkstyle tasks passed. JAR size:
  12,520,434 bytes. The packaged command workflow passed with isolated data,
  including add, name filter, record, correct, repeat, clear, save and reload.
  Initial Checkstyle violations were corrected before the passing run.
- GUI: the packaged application reached UI startup with isolated temporary data,
  but the computer-use tool could not bind its Java window. Visual rendering,
  selection and scrolling are unverified; use the DG manual checklist.
- Integration limit: class/week commands and automatic badge-context wiring still
  require #45. Do not call the integrated MVP complete before those checks pass.
- AI assistance: OpenAI Codex. Reuses the team's existing attendance status,
  class membership, Telegram and AB3 architecture.

## Attendance week dependency

### List command for Feature 3 (2026-10-15)

- Owner: Jian Yang (`jianyang999`); branch: `update-list-command`; milestone: v1.3.
- GitHub issue: [#46](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/46). AI assistance: Claude Code.
- Status: [PR #47](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/47) approved by `Eskalade` with two fixes, now addressed; not merged.
- Scope: `ListCommandParser` rejects any extra input with "Invalid command format!"
  and "Format: list"; `list` reports "{N} students listed!" ("1 student listed!" for one).
  Only surrounding ordinary spaces are accepted: `AddressBookParser` passes `list` its
  raw arguments, so tabs, line breaks and other whitespace are rejected (review fix).
- Validation: after the review fixes and merging team master (with AttendanceWeek),
  `./gradlew check coverage` passed locally on Java 25.0.4.1 with 278 tests, zero
  failures/errors/skips. Non-ASCII spaces next to `list` give "Unknown command."
  because they are not command-word separators; they are still rejected.
- Not yet done (Feature 3 spec): the "All students | {N} students" heading, the
  "No students saved. Use add to create a student." empty-roster text, and clearing
  the selected student and detail panel when `list` succeeds all need UI work;
  coordinate with PR #44, which also changes `PersonListPanel`.
- Class/week `find c/COURSE:GROUP w/WEEK` needs `Person#getMemberships()` from PR #44;
  tracked in issue #45, to be moved to v1.4 if #44 is not merged in time.

### Attendance week (2026-10-15)

- Owner: `toomintyy`; branch: `add-attendance-week`; milestone: v1.3.
- Issue: [#48](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/48).
- PR: [#49](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/49).
- Status: merged into team master in PR #49 (verified 2026-10-15).
- Scope: immutable `AttendanceWeek`, exact Feature 3/4 WEEK validation, canonical
  numeric/display access, equality/hash code, tests and UG/DG documentation.
- Integration: no Person, command, UI or JSON schema changes. Reuse this type for
  future `mark` and `find w/` parsing; attendance keys also need student and course.
- Verified against GitHub on 15 October: attendance status #36, Telegram #28,
  class membership #34 and exit validation #39 are merged. Add-student #44,
  list #47 and delete #40 remain open; #45 tracks class filtering. The older
  increment entries below are dated history, not current remote status.
- Remaining: teammate review and integration of full mark persistence/UI after
  the student memberships in #44. This foundation does not complete the MVP.
- Validation: `./gradlew check coverage shadowJar` passed on Zulu Java 25.0.3:
  271 tests, no failures/errors/skips, Checkstyle passed. AttendanceWeek reached
  100% line and branch coverage. JAR built (12,498,929 bytes); a Java smoke test
  against the packaged class passed for normalized week 1, week 53 and invalid 54.
  Repository checks and `git diff --check` passed. An existing whitespace warning
  and one new test lambda formatting violation were fixed. No GUI behavior changed;
  no manual GUI smoke test was performed.
- AI assistance: OpenAI Codex.

## Current increment: v1.3 add student

- Owner: Vincent Peh (`Eskalade`); branch in fork: `codex/add-student-v13`.
- Issue: [#43](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/43), assigned to Eskalade, v1.3.
- PR: [#44](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/44), v1.3, assigned to Eskalade.
- Status: pushed to fork and open for teammate review. Not merged or released.
- Scope: exact add grammar/field validation, normalization, duplicate contacts,
  immutable memberships, complete JSON round trip, legacy data compatibility,
  save-before-live-model update, selection/details, regression tests and UG/DG.
- Local validation: `./gradlew check coverage shadowJar` passed on Java 25.0.4:
  285 tests, zero failures/errors/skips; both Checkstyle tasks passed; coverage
  reports and JAR generated. Name/Email student validation and AddCommandParser
  have 100% line/branch coverage in this run. `git diff --check` passed.
- Remote validation: [Java CI run](https://github.com/AY2627S1-CS2103T-W08-4/tp/actions/runs/38265061281)
  passed on Linux, Windows and macOS at `95f9d810` (same implementation code as this
  documentation-only validation update). Check the PR for subsequent-head checks.
- Earlier validation failures: ten obsolete AB3-add expectations were updated to
  the new contract; subsequent runs exposed import/formatting violations, now fixed.
- GUI validation: packaged app reached UI startup using an isolated temporary data
  directory. The first sandboxed launch could not access a JavaFX screen; an
  unrestricted launch reached UI startup, but the UI tool could not bind the Java
  window. Selection/rendering have not been visually verified. Smoke processes stopped.
- AI assistance: OpenAI Codex. Reuses team TelegramHandle/ClassMembership and SE-EDU AB3.

## Shared interface and compatibility decisions

Vincent authorized choosing these defaults on 2026-10-12 because no team agreement
was yet recorded. They are implemented on this branch and need teammate PR review.

- Student constructor: `Person(Name, TelegramHandle, Email, Set<ClassMembership>)`.
  `Name.forStudent` / `Email.forStudent` normalize and validate new student data.
- `isStudent()`, `getTelegramHandle(): Optional<TelegramHandle>`, and immutable
  `getMemberships(): Set<ClassMembership>` are available for roster/attendance work.
  Membership order follows input. At least one membership, one group per course.
- Existing phone/address/tag records load and round-trip unchanged as legacy contacts.
  No inferred memberships or fabricated Telegram handles. New students have no legacy
  phone/address; legacy getters return null for students, so guard with `isStudent()`.
- New JSON fields: `telegram`, `memberships`; malformed/incomplete/hybrid student
  records fail validation. Email collisions include legacy contacts case-insensitively.
- Student identity collision uses email OR Telegram; namesakes are allowed. Two legacy
  contacts retain original name identity. The inherited edit command rejects student
  targets rather than discarding fields, and prevents legacy email edits colliding with students.
- Add saves a detached candidate before changing the live model; failed persistence
  leaves the live predicate, data and selection untouched. Disk writes use sibling-temp
  atomic replacement, without a non-atomic fallback. Mark also uses a detached candidate in this branch. Other commands retain their
  existing model-before-save flow; this is not generalized transaction/rollback support.
- All new course/week attendance is implicitly Unrecorded; add creates no attendance
  records. The selected detail area says `No attendance recorded.` until a decision is recorded.
  Attendance owner must integrate actual records and extend model/storage/UI together.
- Class filtering remains Jian Yang's `find c/COURSE:GROUP w/WEEK` work. ClassMembership
  validation is reused; only its provisional error wording was aligned to the original spec.
- Error precedence across categories is incomplete in the supplied spec. Implemented:
  encountered preamble/unknown/repeated prefix errors; missing n,p,e,c; name/Telegram/email;
  all class values; repeated courses in input order. Confirm this in review.
- Source inspected: Vincent's original `CS2103T-W08-4-2.docx`, Feature 1.
  The overview's unique-name claim conflicts with detailed Feature 1; follow namesakes allowed.

## Remaining work

1. Review PR #44; verify CI on its exact head before merge.
2. Teammates review the shared Person/schema interface; integrate their own commands
   without dropping new fields. General persistence/rollback still needs an owner.
3. Complete the DG GUI smoke checklist, including selection, duplicate error input
   retention and restart, before calling the integrated MVP ready.
4. Team completes other MVP features, release packaging/screenshots and milestone wrap-up.
   No release or merge is claimed by this local implementation.

## Add-student contract

Source: the team document `CS2103T-W08-4-2.docx`, Feature 1, supplied by Vincent
on 2026-10-06. The original document is not committed; the summary below preserves
the relevant decisions for implementation. Consult the original for exact messages
and mockups before completing command integration.

Target syntax:

```text
add n/NAME p/TELEGRAM e/EMAIL c/COURSE:GROUP [c/COURSE:GROUP ...]
```

- Prefixes and commands are lowercase and case-sensitive; parameter order is free.
  Trim surrounding ordinary spaces only. `n/`, `p/`, `e/` occur exactly once;
  `c/` occurs at least once. Reject unknown prefixes and repeated singleton prefixes.
- Names: Unicode NFC, collapse ordinary spaces, preserve capitalization, 1–50 code
  points and at least one letter. Allow letters, combining marks, ordinary spaces,
  ASCII/curly apostrophes, hyphens and periods. Names never determine duplicates.
- Telegram: mandatory `@`, followed by 5–32 ASCII characters; first a letter,
  remaining letters/digits/underscores. Store lowercase with `Locale.ROOT`.
  Reject tabs, line breaks, other whitespace, non-ASCII and internal spaces.
  Exact invalid-value message:
  `Telegram handle must start with @, followed by 5-32 letters, digits or underscores, starting with a letter.`
- Email: normalize case; one `@`; 1–64 ASCII local-part characters; total at most
  254; dotted domain. Local part starts/ends alphanumeric, permits `._+-` internally,
  and forbids consecutive periods. Domain labels are 1–63 ASCII alphanumeric/hyphen
  characters, start/end alphanumeric; final label is 2–63 ASCII letters. Do not
  impose a university-domain allowlist.
- Membership: course is 2–4 ASCII letters + 4 digits + optional ASCII letter;
  group is one ASCII letter + 2 digits. Normalize both uppercase. Exactly one colon,
  no internal spaces, no external course catalog. Validate all memberships before
  checking repeated courses in input order. A course may occur only once, even if
  repeated with the same group. Different courses can share a group code.
- Duplicate detection: normalized email OR Telegram matching any existing student
  rejects the entire addition; check email before Telegram, including collisions
  involving two different existing students. Namesakes are allowed; do not merge.
- Successful addition: persist the complete profile, append to roster order, return
  to the full roster and select the new row. Message: `New student added: {NAME}.`
  Attendance initially is Unrecorded; details say `No attendance recorded.`
- Saving failure: retain complete pre-command data and view; never report success.
  Message: `Unable to save changes. No changes were applied. Check available disk space and file permissions, then try again.`

## Increment history

- 2026-10-06: Vincent's Telegram value type, issue #27 / PR #28, merged. Original
  local result: 245 tests passed; 100% line/branch coverage for TelegramHandle.
- 2026-10-08: AttendanceStatus (#35 / #36) and ClassMembership (#33 / #34) merged.
  Authors recorded successful local checks; these were foundation types, not commands.
- 2026-10-09: Aston's Exit argument validation (#37 / #39) merged, replacing unmerged #38.
- 2026-10-10: GitHub v1.2 milestone closed. This does not mean the full MVP is complete.
- 2026-10-12: Vincent published the v1.3 add-student increment, issue #43 / PR #44;
  285 tests and required checks passed; teammate review and merge remain pending.
- 2026-10-06: Vincent's Telegram value-type increment (issue #27, PR #28) merged
  into team master; merge confirmed through GitHub on 2026-10-08. Original local
  validation: Java 25.0.3, 245 tests, zero failures/errors/skips, 100% line/branch
  coverage for `TelegramHandle`. Added this handoff and AGENTS.md. `Person`, add
  parsing, storage and UI integration remain; `p/` still means AB3 phone.
- 2026-10-08: Aston published Exit argument validation in PR #39 for issue #37,
  with UG/DG updates. Java 25.0.4 checks passed: 252 tests, no
  failures/errors/skips; new parser has 100% line/branch coverage. Approved and merged.
- 2026-10-06: Vincent implemented the first Telegram value-type increment; full
  local checks passed. Added this handoff and AGENTS.md. Published issue #27 and
  PR #28 under v1.2; PR #28 was later merged.
- 2026-10-07: Jian Yang implemented the ClassMembership value-type increment for
  view class (issue #33); full local checks passed. Published PR #34 under v1.2.
- 2026-10-08: `toomintyy` implemented the AttendanceStatus increment (issue #35);
  merged in PR #36.
- 2026-10-08: PR #34 approved by `Eskalade`. Merged team master into the branch to
  resolve a CONTEXT.md conflict and recorded the review follow-ups.
- 2026-10-15: Jian Yang updated `list` to the Feature 3 contract (issue #46);
  full local checks passed. Published PR #47 under v1.3.
- 2026-10-15: Ethan adapted the existing `delete INDEX` command to use student
  terminology in its success message, usage text and User Guide. The deletion
  logic remains unchanged; membership and attendance removal need a later increment.
