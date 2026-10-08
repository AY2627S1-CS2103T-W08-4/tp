# TutorTrack team implementation context

Last updated: 2026-10-08 (Singapore time).

This file is a handoff for team members and their coding assistants. Read it with
the code, issues and PRs; it is a snapshot, not proof of the live GitHub state.
Update it in every implementation PR and refresh the status when handing work off.

## Project and workflow

- Team repository: https://github.com/AY2627S1-CS2103T-W08-4/tp
- Product: a local desktop app for teaching staff to manage student contacts,
  course/group memberships and weekly attendance using typed commands.
- Stack: AB3 Java/JavaFX application, Java 25, Gradle, JSON storage.
- Current iteration: v1.2, first functionality increment. Course deadline:
  2026-10-08 at 23:59 SGT. No product release is required for this iteration.
- Create self-assigned issues and PRs under the relevant milestone. Work on a
  separate branch in your fork; obtain teammate review before merging to team master.
- Week 8 instructions:
  https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/project.html
- Grading and expectations:
  https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html
  https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html

## Feature ownership

| MVP feature | Confirmed owner | Status |
| --- | --- | --- |
| Add student | Vincent Peh (`Eskalade`) | First value-type increment merged in PR #28; command integration remains |
| Delete student | Not recorded yet | Coordinate with team |
| List students and view a class | Not recorded yet | Coordinate with team |
| Record and correct attendance | Not recorded yet | Coordinate with team |
| Exit | Aston (`aston-ish`) | Argument validation in open PR #39; awaiting teammate review, unmerged |
| Save and reload | Not recorded yet | Coordinate ownership and integration with team |

Do not infer feature assignments from AboutUs responsibilities such as testing,
documentation or integration. Other members' unpublished work is not known here.

## Current increment

- Owner: Aston (`aston-ish`), assigned Exit by the user on 2026-10-08.
- Branch: `exit-command-validation` in Aston's fork (renamed on 2026-10-08).
- Base: team master `326798ba`, fetched and checked against GitHub on 2026-10-08.
- Status: published in open PR #39; awaiting teammate review, not merged.
  Aston requested that it remain unmerged on 2026-10-08.
- GitHub issue: [#37](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/37),
  `Reject extra arguments in the exit command`, assigned to `aston-ish`, v1.2.
- GitHub PR: [#39](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/39),
  `Reject extra arguments in the exit command`, assigned to `aston-ish`, v1.2.
  Replaces [#38](https://github.com/AY2627S1-CS2103T-W08-4/tp/pull/38), which GitHub
  closed without merging when the source branch was renamed.
- Scope: `ExitCommandParser`, explicit usage feedback, main-parser integration,
  parser and logic regression tests, UG/DG updates and AI acknowledgement.
- Validation: `./gradlew.bat check coverage` passed on Temurin Java 25.0.4, with
  252 tests and zero failures/errors/skips; both main and test Checkstyle passed.
  `ExitCommandParser` has 100% line and branch coverage. `git diff --check` passed.
  Initial runs exposed a filtered-view assumption in a new test helper and one
  lambda-formatting violation; both were corrected before the successful run.
  GUI manual testing has not been performed. PR #38's commit `ad004f1b` passed
  Windows, macOS, Linux and Codecov patch checks before the branch rename.
  The rename and acknowledgement wording edit do not change Java code; verify
  PR #39's checks against its latest commit. Aston will finalize the Developer
  Guide acknowledgement manually; full assistance is recorded in the PR/commits.
- User-visible behavior: lowercase `exit` with surrounding spaces still closes
  the app. Extra arguments such as `exit 3`, `exit anything` and `exit n/Alice`
  produce a format error with `Example: exit`; no exit result is returned and the
  data, saved JSON and current filtered view remain unchanged.
- Integration: implements DG UC05 extension 1a before command execution or saving.
  Existing valid-Exit, save and shutdown paths are retained; no shared model,
  storage schema or teammates' feature branches are changed.
- Remaining work: teammate review, manual GUI validation, graceful shutdown
  verification and full MVP exit/reload integration as dependent features arrive.
  Merge is deferred at Aston's request and requires renewed authorization.

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
- Membership representation is not yet agreed/implemented. Coordinate its public
  interface with roster and attendance owners before changing `Person`.
- Roster indices are positions in the current display, not stable student IDs.
  Attendance is per student/course/week (1–53); Unrecorded is not Absent.
- The document references common error precedence and an interruption appendix
  without providing a complete ordering/recovery contract in the extracted text.
  Resolve ambiguous combinations before implementing their acceptance tests.

## Next steps

1. Verify PR #39's latest remote CI and obtain teammate review. Keep it unmerged
   at Aston's request; only merge after renewed authorization and review.
2. Agree shared student/membership interfaces and legacy-data handling with teammates.
3. Implement Telegram model/parser/storage/UI integration as the next bounded PR.
4. Add name/email contracts, membership support, duplicate checks and atomic save
   behavior in subsequent reviewed increments toward the complete add-student feature.
5. Update user-facing command documentation when behavior changes. Keep planned
   functionality clearly separate from what the application currently supports.

## Increment history

- 2026-10-06: Vincent's Telegram value-type increment (issue #27, PR #28) merged
  into team master; merge confirmed through GitHub on 2026-10-08. Original local
  validation: Java 25.0.3, 245 tests, zero failures/errors/skips, 100% line/branch
  coverage for `TelegramHandle`. Added this handoff and AGENTS.md. `Person`, add
  parsing, storage and UI integration remain; `p/` still means AB3 phone.
- 2026-10-08: Aston published Exit argument validation in PR #38 for issue #37,
  with UG/DG updates and AI credit. Java 25.0.4 checks passed: 252 tests, no
  failures/errors/skips; new parser has 100% line/branch coverage. Review and merge
  remain pending; merge is deferred at Aston's request.
- 2026-10-08: Renamed the source branch to `exit-command-validation` at Aston's
  request. GitHub closed unmerged PR #38; replacement PR #39 retains issue #37,
  the v1.2 milestone and the tested implementation. Shortened the Developer Guide
  acknowledgement for Aston to finalize manually. PR #39 remains unmerged.
