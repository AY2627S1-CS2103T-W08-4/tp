# TutorTrack team implementation context

Last verified: 2026-10-12 (Singapore time). This is a handoff, not a substitute for
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
  atomic replacement, without a non-atomic fallback. Other commands retain their
  existing model-before-save flow; this is not generalized transaction/rollback support.
- All new course/week attendance is implicitly Unrecorded; add creates no attendance
  records. The new selected detail area currently says `No attendance recorded.`
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
