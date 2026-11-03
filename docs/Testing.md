---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: [`seedu.address.commons.util.StringUtilTest`](https://github.com/AY2627S1-CS2103T-W08-4/tp/blob/master/src/test/java/seedu/address/commons/util/StringUtilTest.java)
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`


## Selection regression checks

Run `./gradlew check coverage` (Windows: `gradlew.bat check coverage`) for the full
suite. For direct selection, `SelectCommandParserTest`, `SelectCommandTest` and
`StudentSelectionIntegrationTest` verify displayed indices and read-only behavior.
The select methods in `MvpRosterUiTest` cover detail refresh, command focus and
retention of selection/details/input after errors. Use synthetic data and temporary
storage; packaged-app and cross-platform checks remain separate.


## Student contact regression checks

Run the full `./gradlew check coverage` suite (Windows: `gradlew.bat check coverage`)
before integrating changes. The contact-specific coverage is split across these tests:

| Test class | What it checks |
| --- | --- |
| `StudentValidationTest` | Name/email boundaries, Unicode normalization, locale independence and long pasted inputs |
| `StudentContactParserTest` | Add/update grammar, parameter order, invalid whitespace and large input |
| `StudentContactAcceptanceTest` | Contact limits, normalized no-op, collisions, save failure/retry and JSON reload |
| `StudentUpdateIntegrationTest` | Filtered indices, hidden collisions and preservation of memberships/attendance |
| `MvpRosterUiTest` | Selection, details, badges, class/week heading and retained input on failure |

### Packaged add/update acceptance

Run these checks on the **exact JAR proposed for release**, from a new disposable
folder with synthetic data only. Copy the JAR into that folder before launching:
v1.6 saves beside the JAR even when invoked from another directory.
Record the JAR SHA-256, source revision, OS and
`java -version`. Do not reuse your real TutorTrack data folder: `clear` below deletes
all records without undo. Automated GUI tests do not replace this packaged check.

Enter one command at a time. This sequence deliberately puts a conflicting student
outside the displayed class, so duplicate checks must search the full roster.

| Step | Command or action | Expected result |
| --- | --- | --- |
| 1 | `clear` | Empty roster; all sample entries removed from this disposable dataset |
| 2 | `add n/Anne-Marie Tan p/@Anne_Tan e/Anne@Example.com c/cs2103t:t04 c/MA2001:T02` | Anne selected in the full roster; lowercase contacts, uppercase classes and no attendance |
| 3 | `add n/Bob Lim p/@bob_lim e/bob@example.com c/CS2103T:T05` | Bob appended as row 2 and selected |
| 4 | `find c/CS2103T:T04 w/3` then `mark 1 m/CS2103T w/3 s/present` | Only Anne displayed; Present badge and week-3 history |
| 5 | `update 1 n/Priya d/o Raman p/@Priya_New e/priya@example.com` | Name/contact details refreshed; class/week, both memberships and attendance preserved |
| 6 | `update 1 e/bob@example.com` | Duplicate-email error despite Bob being hidden; original contact, selection, attendance and attempted input retained |
| 7 | `update 1 p/@BOB_LIM` | Duplicate-Telegram error; no data/view change |
| 8 | `update 1 p/@PRIYA_NEW e/PRIYA@EXAMPLE.COM` | `Student unchanged: Priya d/o Raman.`; selection retained |
| 9 | `update 1 p/` then `update 0 n/Alice` | Both rejected; required contact cannot be erased and index zero is invalid |
| 10 | `exit`, restart from the same folder, then `find c/CS2103T:T04 w/3` and `select 1` | Updated contacts, both memberships and Present attendance survive restart |
| 11 | `add n/Priya d/o Raman p/@anne_tan e/anne@example.com c/CS1101S:T02` | Same name accepted with the first student's now-released contacts; new profile has no attendance |
| 12 | Repeat step 11 | Duplicate-email error; no extra row is created |

For save-failure recovery, use the injected-storage tests in
`StudentContactAcceptanceTest` and `StudentUpdateIntegrationTest`. For an additional
packaged check, use a disposable directory whose file permissions prevent replacement
of the data file, attempt a changed update, restore permissions and retry. Expect the
save-failure message and unchanged data/view before retry, then persistence after
retry and restart. Record when the OS or account privileges prevent reproducing this
condition; changing only the JSON file's write bit may not prevent atomic replacement.
A normalized no-op should still succeed without writing.
