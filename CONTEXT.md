# TutorTrack team implementation context

Last updated: 2026-10-07 (Singapore time).

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
| Add student | Vincent Peh (`Eskalade`) | First increment (Telegram handle) merged in PR #28 |
| Delete student | Not recorded yet | Coordinate with team |
| List students and view a class | Jian Yang (`jianyang999`) | First increment (class membership) in progress, issue #33 |
| Record and correct attendance | Not recorded yet | Coordinate with team |
| Save, reload and exit | Not recorded yet | Coordinate with team |

Do not infer feature assignments from AboutUs responsibilities such as testing,
documentation or integration. Other members' unpublished work is not known here.

## Current increment

- Owner: Jian Yang (`jianyang999`).
- Branch: `add-class-membership`.
- Base: team master `326798ba`, fetched on 2026-10-07.
- Status: local implementation; PR not yet opened.
- GitHub issue: [#33](https://github.com/AY2627S1-CS2103T-W08-4/tp/issues/33),
  `Add class membership value type for viewing a class`, assigned to `jianyang999`, v1.2.
- Scope: immutable `ClassMembership` (course code + tutorial group, `COURSE:GROUP`),
  validation per the membership rules in the add-student contract below, uppercase
  normalization with `Locale.ROOT`, value equality/hashing, `isSameCourse`, automated
  tests and DG explanation.
- Code: `src/main/java/seedu/address/model/person/ClassMembership.java`.
- Tests: `src/test/java/seedu/address/model/person/ClassMembershipTest.java`.
- Documentation: DG Implementation section and AI acknowledgement.
- Validation: `./gradlew check coverage` passed on Java 25.0.4.1, with 252 tests,
  zero failures/errors/skips. ClassMembership has 100% line and branch coverage.
  This is local validation; check the PR for the latest remote CI results.
- User-visible behavior: unchanged. `Person`, parsing, JSON storage and UI are not
  connected to the new type yet.
- Open point: `MESSAGE_CONSTRAINTS` wording is provisional; align it with the exact
  message in the team specification document if one is given there.

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

1. Obtain teammate review of the ClassMembership PR (issue #33) and verify remote CI
   before merging. Do not mark this increment merged until it actually is.
2. Agree shared student/membership interfaces and legacy-data handling with teammates,
   including how `Person` stores its `ClassMembership` set.
3. Implement Telegram model/parser/storage/UI integration as the next bounded PR.
4. Add name/email contracts, membership support, duplicate checks and atomic save
   behavior in subsequent reviewed increments toward the complete add-student feature.
5. View class (v1.3): store memberships on `Person`, then add a class predicate and a
   `view` command that filters the roster by `COURSE:GROUP`, with week/attendance
   display once attendance exists.
6. Update user-facing command documentation when behavior changes. Keep planned
   functionality clearly separate from what the application currently supports.

## Increment history

- 2026-10-06: Vincent implemented the first Telegram value-type increment; full
  local checks passed. Added this handoff and AGENTS.md. Published issue #27 and
  PR #28 under v1.2; PR #28 was later merged.
- 2026-10-07: Jian Yang implemented the ClassMembership value-type increment for
  view class (issue #33); full local checks passed.
