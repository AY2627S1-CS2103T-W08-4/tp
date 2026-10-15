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
- Current iteration: v1.3, working MVP. Course deadline:
  2026-10-15 at 23:59 SGT; a release with JAR, release notes and screenshots is required.
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
| Add student | Vincent Peh (`Eskalade`) | First increment (Telegram handle) merged in PR #28 |
| Delete student | Not recorded yet | Coordinate with team |
| List students and view a class | Jian Yang (`jianyang999`) | ClassMembership merged in PR #34; `list` update in PR #47, awaiting review |
| Record and correct attendance | `toomintyy` (confirmed in owner conversation) | AttendanceStatus (#36) and AttendanceWeek (#49) merged |
| Exit | Aston (`aston-ish`) | Argument validation merged in PR #39 |
| Save and reload | Not recorded yet | Coordinate with team |

Do not infer feature assignments from AboutUs responsibilities such as testing,
documentation or integration. Other members' unpublished work is not known here.

## Current increment

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

### Exit argument validation (2026-10-08)

- Owner: `aston-ish`; Branch: `exit-command-validation`; milestone: v1.2.
- Status: approved by Jian Yang, not merged.
- GitHub issue: [#37](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/37),
  `Reject extra arguments in the exit command`, assigned to `aston-ish`, v1.2.
- GitHub PR: [#39](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/39),
  `Reject extra arguments in the exit command`, assigned to `aston-ish`, v1.2.
  Replaces [#38](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/38), which GitHub
  closed without merging when the source branch was renamed to fit the MVP feature.
- Scope: `ExitCommandParser`, explicit usage feedback, main-parser integration,
  parser and logic regression tests, UG/DG updates and AI acknowledgement.
- Validation: `./gradlew.bat check coverage` passed on Temurin Java 25.0.4, with
  252 tests and zero failures/errors/skips; both main and test Checkstyle passed.
  `ExitCommandParser` has 100% line and branch coverage. `git diff --check` passed.
  Initial runs exposed a filtered-view assumption in a new test helper and one
  lambda-formatting violation; both were corrected before the successful run.
  GUI manual testing has not been performed. PR #38's commit `ad004f1b` passed
  Windows, macOS, Linux and Codecov patch checks before the branch rename.
- User-visible behavior: lowercase `exit` with surrounding spaces still closes
  the app. Extra arguments such as `exit 3`, `exit anything` and `exit n/Alice`
  produce a format error with `Example: exit`; no exit result is returned and the
  data, saved JSON and current filtered view remain unchanged.
- Integration: implements DG UC05 extension 1a before command execution or saving.
  Existing valid-Exit, save and shutdown paths are retained; no shared model,
  storage schema or teammates' feature branches are changed.
- Remaining work: teammate review, manual GUI validation, graceful shutdown
  verification and full MVP exit/reload integration as dependent features arrive.

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
