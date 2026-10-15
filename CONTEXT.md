# TutorTrack team implementation context

Last updated: 2026-10-15 (Singapore time).

This file is a handoff for team members and their coding assistants. Read it with
the code, issues and PRs; it is a snapshot, not proof of the live GitHub state.
Update it in every implementation PR and refresh the status when handing work off.

## Project and workflow

- Team repository: https://github.com/AY2627S1-CS2103T-W08-4/tp
- Product: a local desktop app for teaching staff to manage student contacts,
  course/group memberships and weekly attendance using typed commands.
- Stack: AB3 Java/JavaFX application, Java 25, Gradle, JSON storage.
- Current iteration: v1.3, first working MVP release. Course deadline:
  2026-10-15 at 23:59 SGT; the team must publish the JAR, release notes and screenshots.
- Create self-assigned issues and PRs under the relevant milestone. Work on a
  separate branch in your fork; obtain teammate review before merging to team master.
- Week 9 instructions:
  https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/project.html
- Grading and expectations:
  https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html
  https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html

## Feature ownership

| MVP feature | Confirmed owner | Status |
| --- | --- | --- |
| Add student | Vincent Peh (`Eskalade`) | Telegram type merged in PR #28; full student flow in open PR #44 |
| Delete student | Not recorded yet | Coordinate with team |
| List students and view a class | Jian Yang (`jianyang999`) | Class membership merged in PR #34; list work in open PR #47 |
| Record and correct attendance | `toomintyy` (confirmed in owner conversation) | Status type merged in PR #36; commands/persistence in open PR #51 |
| Exit | Aston (`aston-ish`) | Validation merged in PR #39; v1.3 lifecycle fixes verified locally for issue #52, unmerged |
| Save and reload | Not recorded yet | Coordinate with team |

Do not infer feature assignments from AboutUs responsibilities such as testing,
documentation or integration. Other members' unpublished work is not known here.

## Current increment

### Exit handling and verification (2026-10-15)

- Owner: Aston (`aston-ish`); branch: `exit-handling-verification`; milestone: v1.3.
- Base: team master `b3e1cf4d`, fetched and verified on GitHub on 2026-10-15.
- Issue: [#52](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/52).
- Status: implemented and verified locally; no PR opened, no merge performed.
- Scope: route typed/menu/window-close Exit through saving; block closing on save
  failure; show an actionable wrapped error; preserve both windows until save
  succeeds; retain argument validation. Add 11 logic/real-window GUI tests, UG/DG
  updates and accurate AI credit. Linux GUI CI uses Xvfb; Gradle dependencies are unchanged.
- Branch validation: `./gradlew.bat check coverage shadowJar` passed on Temurin
  Java 25.0.4 on Windows: 276 tests, zero failures/errors/skips, both Checkstyle
  tasks and coverage passed. Tests cover IO/permission errors, failure/retry,
  state/file preservation, all three close routes, open Help and legacy reload.
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
### Class membership for view class (2026-10-08)

- Owner: Jian Yang (`jianyang999`); branch: `add-class-membership`; milestone: v1.2.
- AI assistance: Claude Code.
- Status: merged into team master in PR #34 (verified 2026-10-08).
- GitHub PR: [#34](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/34),
  `Add class membership value type`, assigned to `jianyang999`, v1.2.
- GitHub issue: [#33](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/33),
  `Add class membership value type for viewing a class`, assigned to `jianyang999`, v1.2.
- Scope: immutable `ClassMembership` (course code + tutorial group, `COURSE:GROUP`),
  validation per the membership rules in the add-student contract below, uppercase
  normalization with `Locale.ROOT`, value equality/hashing, `isSameCourse`, automated
  tests and DG explanation.
- Code: `src/main/java/seedu/address/model/person/ClassMembership.java`.
- Tests: `src/test/java/seedu/address/model/person/ClassMembershipTest.java`.
- Documentation: DG Implementation section and AI acknowledgement.
- Validation: after merging team master (with AttendanceStatus), `./gradlew check
  coverage` passed locally on Java 25.0.4.1 with 258 tests, zero failures/errors/skips.
  ClassMembership has 100% line and branch coverage. Check the PR for remote CI.
- User-visible behavior: unchanged. `Person`, parsing, JSON storage and UI are not
  connected to the new type yet.
- Open point: `MESSAGE_CONSTRAINTS` wording is provisional; align it with the exact
  message in the team specification document if one is given there.
- Target command (per review): extend `find` as `find c/COURSE:GROUP w/WEEK`
  rather than adding a separate `view` command.

### Attendance status (2026-10-08)

- Owner: `toomintyy`; branch: `35-add-attendance-status`; milestone: v1.2.
- Status: merged into team master in PR #36 (verified 2026-10-08). AI assistance: Codex.
- GitHub issue: [#35](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/35),
  `Add attendance status type for attendance tracking`, assigned to `toomintyy`, v1.2.
- Scope: `AttendanceStatus` enum in `model.person` and six JUnit tests.
  Converts case-insensitive status words, trims U+0020 spaces only, rejects invalid
  input with the specified message, and provides canonical display labels.
- Source: Feature 4 STATUS contract in the owner's current MVP Markdown export.
- Validation: focused `AttendanceStatusTest` passed; final `./gradlew check coverage`
  passed, including Checkstyle and all 268 tests with no failures/errors/skips.
- Integration: no Person, command, storage or UI changes. Attendance remains per
  student/course/week; the enum alone does not record or clear attendance.
- Remaining: coordinate memberships and persistence before implementing the `mark` command.

### Telegram increment

- Owner: Vincent Peh (`Eskalade`). `TelegramHandle` value type, issue #27, merged in
  PR #28. Not yet connected to `Person`, parsing, storage or UI.

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

## Shared decisions and integration risks

- The specification overview says names are unique, but Feature 1 explicitly allows
  namesakes. Follow the detailed contract, consistent with the existing DG; flag the
  overview inconsistency when the team next revises its specification.
- Existing AB3 `Person.isSamePerson` compares names; it must evolve alongside contact
  uniqueness. Existing `Name` and `Email` validation do not implement the full contract.
- Existing add requires phone/address. Do not silently treat old phone numbers as
  Telegram handles or fabricate handles for old/sample data. Agree a data migration
  or compatibility policy before changing the schema.
- Replacing `p/` affects parsers, fixtures, samples, help, UI and existing commands.
  Ensure edits preserve new fields even where editing is beyond the MVP scope.
- `LogicManager` currently mutates the model before saving and does not roll back
  on failure. Coordinate with the persistence owner to meet the atomicity contract.
- `ClassMembership` (issue #33) is the proposed shared membership value type for
  add-student, view-class and attendance. How `Person` stores memberships is not yet
  agreed; coordinate with the add-student and attendance owners before changing `Person`.
- Roster indices are positions in the current display, not stable student IDs.
  Attendance is per student/course/week (1–53); Unrecorded is not Absent.
- The document references common error precedence and an interruption appendix
  without providing a complete ordering/recovery contract in the extracted text.
  Resolve ambiguous combinations before implementing their acceptance tests.

## Next steps

1. Keep each increment's status accurate: verify reviews, CI and merges on GitHub
   before recording them here.
   Exit issue #52 is locally verified but still needs its PR, review and remote CI.
2. Agree shared student/membership interfaces and legacy-data handling with teammates,
   including how `Person` stores its `ClassMembership` set.
3. Implement Telegram model/parser/storage/UI integration as the next bounded PR.
4. Add name/email contracts, membership support, duplicate checks and atomic save
   behavior in subsequent reviewed increments toward the complete add-student feature.
5. View class (v1.3): store memberships on `Person`, then add a class predicate and
   extend `find` as `find c/COURSE:GROUP w/WEEK` to filter the roster by class and show
   attendance for that week. Align `ClassMembership.MESSAGE_CONSTRAINTS` with the exact
   class-format error in the MVP specification first (review follow-up on PR #34).
6. Update user-facing command documentation when behavior changes. Keep planned
   functionality clearly separate from what the application currently supports.

## Increment history

- 2026-10-06: Vincent's Telegram value-type increment (issue #27, PR #28) merged
  into team master; merge confirmed through GitHub on 2026-10-08. Original local
  validation: Java 25.0.3, 245 tests, zero failures/errors/skips, 100% line/branch
  coverage for `TelegramHandle`. Added this handoff and AGENTS.md. `Person`, add
  parsing, storage and UI integration remain; `p/` still means AB3 phone.
- 2026-10-08: Aston published Exit argument validation in PR #39 for issue #37,
  with UG/DG updates. Java 25.0.4 checks passed: 252 tests, no
  failures/errors/skips; new parser has 100% line/branch coverage. PR #39 merged
  on 2026-10-09; verified through GitHub on 2026-10-15.
- 2026-10-06: Vincent implemented the first Telegram value-type increment; full
  local checks passed. Added this handoff and AGENTS.md. Published issue #27 and
  PR #28 under v1.2; PR #28 was later merged.
- 2026-10-07: Jian Yang implemented the ClassMembership value-type increment for
  view class (issue #33); full local checks passed. Published PR #34 under v1.2.
- 2026-10-08: `toomintyy` implemented the AttendanceStatus increment (issue #35);
  merged in PR #36.
- 2026-10-08: PR #34 approved by `Eskalade`. Merged team master into the branch to
  resolve a CONTEXT.md conflict and recorded the review follow-ups.
- 2026-10-15: Aston completed local Exit lifecycle/failure verification for issue
  #52: 276 branch tests and 323 combined-dependency tests passed, plus a packaged
  two-process restart smoke test. The feature branch and dependency PRs remain
  unmerged; review, remote CI and final integrated release checks remain.
