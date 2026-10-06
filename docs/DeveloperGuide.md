---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Vincent Peh used OpenAI Codex to help interpret the team's add-student specification,
  implement and test the Telegram handle value type, and draft its developer documentation.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Add-student foundation: Telegram handles

`TelegramHandle` is an immutable value type for the add-student feature. It removes
surrounding ordinary spaces (U+0020), requires `@` followed by 5–32 ASCII characters,
and requires the first character after `@` to be a letter. Subsequent characters may
be letters, digits or underscores. Tabs, line breaks, internal spaces and non-ASCII
characters are rejected. This is TutorTrack's supported subset, not a statement of
all Telegram username rules.

Accepted handles are stored in lowercase using `Locale.ROOT`, so normalization does
not depend on the computer's language settings. Equality and hashing use this stored
value: `@Alice_1` and ` @alice_1 ` represent the same handle. Invalid construction
throws `IllegalArgumentException` with `MESSAGE_CONSTRAINTS`; null input is a
programming error and throws `NullPointerException`.

This increment introduces the value type and its automated tests only. It is not yet
connected to `Person`, commands, storage or the UI; the existing `add` command still
uses AB3's phone field. Follow-up increments will use this type for the MVP's `p/`
Telegram parameter and contact uniqueness checks. No new user command is available
in this increment.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**: Teaching assistants (TAs) and professors teaching computer
science courses who manage students across multiple courses and tutorial groups.
They are comfortable with technology, type quickly, prefer command-based workflows,
and use a desktop computer for their teaching administration. Each TutorTrack
installation manages one teaching staff member's local records.

**Value proposition**: Bring student contacts, course and class membership, and
teaching records into one place so that tech-savvy teaching staff can spend less
time on administration using fast typed commands.

**Persona**: John is a CS TA teaching multiple courses and classes. He struggles to
keep track of his students, is highly comfortable with technology, and wants to
view their details at a glance while spending less time on administrative work.

The following requirements describe the intended product, not features already
implemented. Priorities express the team's ordering of work; they are not course
iteration numbers. The course's v1.1 iteration is documentation-only.

**MVP scope**: Add a student with contact details and one or more course/group
memberships; delete students; list all students or view one class for a chosen
week; record, correct or clear attendance; and save, reload and exit. Editing,
GitHub usernames, project teams, participation points, tags, Telegram search,
bulk attendance and absence analytics are beyond this MVP.

**Domain rules from the MVP specification**:

* One dataset covers one teaching staff member's students for one semester.
* A student has one profile and at most one tutorial group per course.
* Normalized email addresses and Telegram handles are individually unique.
  Namesakes are permitted under the detailed add-student contract.
* A class is identified by its course and group together, such as CS2103T:T04.
* Attendance belongs to a student, course and teaching week (1–53). It is Present,
  Absent or Unrecorded. Unrecorded means no decision, not an absence.
* The MVP supports one attendance decision per course per week, with no automatic
  calendar inference or separate sessions within that week.
* Displayed indices refer to the current roster, not permanent student IDs.

### User stories

Priorities: **P1** foundational student management; **P2** course and class
organization; **P3** tracking and grading support; **P4** stretch goals.
The complete set below includes requirements beyond the MVP.

| ID | Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- | --- |
| S01 | P1 | CS TA | add a student with their name, university email and Telegram handle | keep their core contact details together |
| S02 | P1 | CS TA | add a student's GitHub username to their profile | quickly find their repositories for lab grading |
| S03 | P1 | TA | delete a student | remove students who have dropped the course |
| S04 | P1 | TA | edit a student's details | keep their email and Telegram handle up to date |
| S05 | P1 | professor | list all my students | get an overview of everyone I teach |
| S06 | P2 | TA teaching multiple courses | assign a student to a course code | distinguish students from different courses |
| S07 | P2 | TA | assign a student to a tutorial group within a course | organize students by the classes I teach |
| S08 | P2 | TA | filter students by course and tutorial group | see the students attending a particular class |
| S09 | P2 | CS professor | assign students to project teams within a course | track group assignments and team-based grading |
| S10 | P3 | TA | mark a student present or absent for a tutorial session using a text command | record attendance quickly during class |
| S11 | P3 | TA | add participation points to a student | record class contributions before I forget |
| S12 | P3 | CS TA | tag a student with programming weaknesses | identify students who need extra attention in labs |
| S13 | P3 | TA | search for a student by Telegram handle | identify the student and their class when they message me |
| S14 | P4 | TA | list students who have missed more than two tutorials | identify students to contact with a warning email |
| S15 | P4 | professor | mark an entire tutorial group present for a session, then correct individual absentees | take attendance with fewer commands |
| S16 | P4 | TA | clear all student records with one purge command | prepare the application for a new semester |

### Use cases

**System**: TutorTrack. **Actor**: a TA or professor (called the user below).
MSS means main success scenario. These use cases describe intended behavior rather
than final command syntax. Course context distinguishes groups with the same name.

#### UC01: Add a student (S01)

**MSS**

1. The user requests to add a student and supplies their name, university email, Telegram handle and at least one course/group membership.
2. TutorTrack validates the supplied details and creates the student record.
3. TutorTrack shows confirmation and the saved student details.

Use case ends.

**Extensions**

* 2a. Required details are missing or invalid.
  * 2a1. TutorTrack identifies the invalid fields without creating a record.
  * 2a2. The user supplies corrected details.
  * Use case resumes at step 2.
* 2b. The normalized email or Telegram handle already belongs to a student record.
  * 2b1. TutorTrack reports the conflicting email or Telegram handle without creating a duplicate.
  * Use case ends.

* 2c. Saving fails.
  * 2c1. TutorTrack reports the failure without creating a student or changing the previous view.
  * Use case ends.

#### UC02: View a class roster (S05, S08)

**MSS**

1. The user requests students for a course, tutorial group and teaching week.
2. TutorTrack displays the matching students and their attendance for that course and week.
3. The user requests the full roster.
4. TutorTrack removes the filter and displays all students once each.

Use case ends.

**Extensions**

* 1a. The class or week input is missing or invalid.
  * 1a1. TutorTrack explains the error and retains the previous view.
  * Use case resumes at step 1.
* 2a. The class has no students.
  * 2a1. TutorTrack shows an empty roster with the requested class and week context.
  * Use case resumes at step 3.
* 2b. A student has no attendance decision for the requested course and week.
  * 2b1. TutorTrack shows Unrecorded for that student.
  * Use case resumes at step 3.

#### UC03: Record and correct attendance (S08, S10)

**MSS**

1. The user requests the roster for a course, tutorial group and teaching week.
2. TutorTrack shows the students in that group.
3. The user specifies a student, enrolled course, teaching week and attendance status.
4. TutorTrack saves the attendance and shows the resulting record.
5. The user notices an incorrect status and requests a correction for the same student, course and week.
6. TutorTrack updates the existing attendance record and shows the corrected status.

Use case ends.

**Extensions**

* 2a. The roster is empty.
  * Use case ends.
* 3a. The student index, course, week or attendance status is invalid, or the student is not enrolled in the specified course.
  * 3a1. TutorTrack explains the problem without changing attendance.
  * Use case resumes at step 3.
* 3b. The requested status is already recorded.
  * 3b1. TutorTrack reports that attendance is unchanged, without adding a record.
  * Use case ends.
* 4a. Saving the initial attendance decision fails.
  * 4a1. TutorTrack reports the failure and retains the complete pre-command data and view.
  * Use case ends.
* 5a. No correction is needed.
  * Use case ends.
* 5b. The correction details are invalid.
  * 5b1. TutorTrack explains the problem and retains the previous record.
  * Use case resumes at step 5.
* 5c. The user clears a mistaken record by specifying Unrecorded.
  * 5c1. TutorTrack saves removal of the decision for that student, course and week; on a save failure, extension 6a applies.
  * Use case ends.
* 6a. Saving the correction fails.
  * 6a1. TutorTrack reports the failure and retains the previous attendance and view.
  * Use case ends.

#### UC04: Delete students (S03)

**MSS**

1. The user requests the full roster or a class roster.
2. TutorTrack displays the matching students.
3. The user specifies one or more displayed student indices to delete.
4. TutorTrack resolves all targets against the pre-command roster and saves their removal, including memberships and attendance.
5. TutorTrack confirms the deletions and updates the roster while preserving its filter.

Use case ends.

**Extensions**

* 2a. The roster is empty.
  * Use case ends.
* 3a. Any index has invalid syntax or is outside the displayed roster.
  * 3a1. TutorTrack reports the error without deleting any students.
  * Use case resumes at step 2.
* 4a. Saving fails.
  * 4a1. TutorTrack reports the failure and retains all pre-command records and the previous view.
  * Use case ends.

#### UC05: Exit and reload

**MSS**

1. The user requests to exit TutorTrack.
2. TutorTrack waits for any in-progress command to finish, then closes.
3. The user launches TutorTrack again.
4. TutorTrack loads all previously saved student, membership and attendance records.

Use case ends.

**Extensions**

* 1a. The exit command contains extra arguments.
  * 1a1. TutorTrack explains the correct format and keeps the application open.
  * Use case resumes at step 1.

### Non-Functional Requirements

1. TutorTrack shall run on Windows, Linux and macOS with Java 25, without requiring a separate installer.
2. Student, membership and teaching records shall be stored locally in human-editable text files. Normal use shall not depend on a database server or a remote service.
3. Each installation shall support a single user's records. Shared multi-user access and synchronization are outside the intended scope.
4. Core student-management, course-organization and attendance operations shall be executable through typed commands without requiring mouse interaction.
5. The interface shall remain usable at 1920 × 1080 with 100% and 125% scaling, and at 1280 × 720 with 150% scaling.
6. Distribution shall use a single JAR no larger than 100 MB, including dependencies.
7. Invalid command input shall produce actionable feedback and shall not change stored records.
8. Successfully saved records shall remain available after the application is closed and reopened.

These are acceptance targets for the intended product. Platform, display, persistence
and invalid-input behavior must be verified as the features are implemented.

### Glossary

* **Course**: A subject taught under a course code, such as CS2103T; also called a module.
* **Course membership**: The association between a student and a course.
* **Tutorial group**: A class within a course, such as T04. The course and group together identify the class.
* **Teaching week**: An integer from 1 to 53 agreed by the operator for the semester; not inferred from calendar dates.
* **Attendance record**: A student's present or absent status for a particular course and teaching week. A missing record does not by itself mean absent.
* **Participation points**: A teaching staff member's numerical record of a student's class contributions.
* **Project team**: A group of students working on an assignment within a course.
* **Programming weakness tag**: A label describing a topic where a student may need additional help.
* **University email**: A student's university-issued email address.
* **Telegram handle**: A Telegram username used to identify and contact a student.
* **TA**: Teaching assistant.
* **CLI**: Command-line interface; interaction by typing commands.
* **MVP**: Minimum viable product; the smallest agreed feature set that provides useful value to the target user.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
