package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.AttendanceTestData.ALICE;
import static seedu.address.testutil.AttendanceTestData.BOB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AttendanceIntegrationTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private CountingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        storage = new CountingStorage(directory.resolve("students.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void mark_correctClearAndRestart_preservesIndependentDecisions() throws Exception {
        CommandResult result = logic.execute("mark 1 m/CS2103T w/1 s/present");
        assertEquals("Attendance set: Alice | CS2103T | Week 1 | Present.", result.getFeedbackToUser());
        assertEquals(model.getFilteredPersonList().getFirst(), result.getSelectedPerson().orElseThrow());
        logic.execute("mark 1 m/CS1101S w/1 s/absent");
        logic.execute("mark 1 m/CS2103T w/2 s/present");
        logic.execute("mark 2 m/CS2103T w/1 s/absent");
        logic.execute("mark 1 m/CS2103T w/1 s/absent");
        Person alice = model.getFilteredPersonList().getFirst();
        assertEquals(3, alice.getAttendance().size());
        assertEquals(AttendanceStatus.ABSENT, alice.getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
        logic.execute("mark 1 m/CS2103T w/1 s/unrecorded");
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        Person savedAlice = reloaded.getPersonList().getFirst();
        assertEquals(2, savedAlice.getAttendance().size());
        assertEquals(AttendanceStatus.UNRECORDED, savedAlice.getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
        assertEquals(AttendanceStatus.ABSENT, savedAlice.getAttendanceStatus("CS1101S", new AttendanceWeek("1")));
        assertEquals(AttendanceStatus.PRESENT, savedAlice.getAttendanceStatus("CS2103T", new AttendanceWeek("2")));
        assertEquals(AttendanceStatus.ABSENT,
                reloaded.getPersonList().get(1).getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
        ModelManager restarted = new ModelManager(reloaded, new UserPrefs());
        LogicManager restartedLogic = new LogicManager(restarted, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        assertEquals("Attendance unchanged: Alice | CS1101S | Week 1 is already Absent.",
                restartedLogic.execute("mark 1 m/CS1101S w/1 s/absent").getFeedbackToUser());
    }

    @Test
    public void mark_filteredIndex_preservesFilterOrderAndSelectsTarget() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(BOB.getName()));
        CommandResult result = logic.execute("mark 1 m/CS2103T w/53 s/present");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(BOB.getName(), result.getSelectedPerson().orElseThrow().getName());
        assertEquals(ALICE, model.getAddressBook().getPersonList().getFirst());
        assertEquals(result.getSelectedPerson().orElseThrow(), model.getAddressBook().getPersonList().get(1));
        assertThrows(CommandException.class, MarkCommand.INVALID_INDEX, () ->
                logic.execute("mark 2 m/CS2103T w/1 s/present"));
        model.updateFilteredPersonList(person -> true);
        assertEquals(List.of(ALICE.getName(), BOB.getName()),
                model.getFilteredPersonList().stream().map(Person::getName).toList());
    }

    @Test
    public void mark_repeatedStatuses_neverSaveButReturnSelection() throws Exception {
        assertEquals("Attendance unchanged: Alice | CS2103T | Week 1 is already Unrecorded.",
                logic.execute("mark 1 m/CS2103T w/1 s/unrecorded").getFeedbackToUser());
        assertEquals(0, storage.saves);
        logic.execute("mark 1 m/CS2103T w/1 s/present");
        Person selected = model.getFilteredPersonList().getFirst();
        storage.fail = true;
        CommandResult result = logic.execute("mark 1 m/CS2103T w/1 s/PRESENT");
        assertEquals("Attendance unchanged: Alice | CS2103T | Week 1 is already Present.", result.getFeedbackToUser());
        assertSame(selected, result.getSelectedPerson().orElseThrow());
        assertEquals(1, storage.saves);
    }

    @Test
    public void mark_failedSave_keepsOriginalObjectsViewAndFile() throws Exception {
        logic.execute("mark 2 m/CS2103T w/1 s/present");
        String before = Files.readString(storage.getAddressBookFilePath());
        model.updateFilteredPersonList(person -> person.getName().equals(BOB.getName()));
        Person target = model.getFilteredPersonList().getFirst();
        List<Person> roster = List.copyOf(model.getAddressBook().getPersonList());
        storage.fail = true;
        for (String status : List.of("absent", "unrecorded")) {
            assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () ->
                    logic.execute("mark 1 m/CS2103T w/1 s/" + status));
            assertSame(target, model.getFilteredPersonList().getFirst());
            assertEquals(List.of(target), model.getFilteredPersonList());
            assertEquals(roster, model.getAddressBook().getPersonList());
            assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void mark_failedInitialSaveThenRetry_persistsOnlyTargetDecision() throws Exception {
        assertFailedSaveThenRetry(AttendanceStatus.UNRECORDED, AttendanceStatus.PRESENT);
    }

    @Test
    public void mark_failedCorrectionThenRetry_persistsOnlyTargetDecision() throws Exception {
        assertFailedSaveThenRetry(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT);
    }

    @Test
    public void mark_failedClearThenRetry_persistsOnlyTargetDecision() throws Exception {
        assertFailedSaveThenRetry(AttendanceStatus.PRESENT, AttendanceStatus.UNRECORDED);
    }

    private void assertFailedSaveThenRetry(AttendanceStatus initial, AttendanceStatus requested) throws Exception {
        logic.execute("mark 1 m/CS2103T w/53 s/absent");
        logic.execute("mark 1 m/CS1101S w/1 s/present");
        logic.execute("mark 2 m/CS2103T w/1 s/absent");
        logic.execute("mark 1 m/CS2103T w/1 s/" + initial);
        logic.execute("find c/CS1101S:T02 w/1");
        Person target = model.getFilteredPersonList().getFirst();
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        List<String> changes = new ArrayList<>();
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> changes.add("changed"));
        String command = "mark 1 m/CS2103T w/1 s/" + requested;
        int attempts = storage.saves;

        storage.fail = true;
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute(command));
        assertEquals(attempts + 1, storage.saves);
        assertEquals(before, model.getAddressBook().getPersonList());
        assertEquals(List.of(target), model.getFilteredPersonList());
        assertSame(target, model.getFilteredPersonList().getFirst());
        assertTrue(changes.isEmpty());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));

        storage.fail = false;
        CommandResult result = logic.execute(command);
        Person expected = target.withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("1"), requested));
        assertEquals(attempts + 2, storage.saves);
        assertEquals("Attendance set: Alice | CS2103T | Week 1 | " + requested + ".", result.getFeedbackToUser());
        assertEquals(expected, result.getSelectedPerson().orElseThrow());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        List<Person> expectedRoster = List.of(expected, before.get(1));
        assertEquals(expectedRoster, model.getAddressBook().getPersonList());
        assertEquals(expectedRoster, storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(requested, expected.getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
        assertEquals(initial, target.getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
    }

    @Test
    public void mark_afterContactUpdate_correctsExistingDecisionAndReloads() throws Exception {
        logic.execute("mark 1 m/CS2103T w/1 s/present");
        logic.execute("mark 1 m/CS2103T w/53 s/absent");
        logic.execute("mark 1 m/CS1101S w/1 s/present");
        logic.execute("mark 2 m/CS2103T w/1 s/present");
        Person other = model.getAddressBook().getPersonList().get(1);
        logic.execute("find c/CS1101S:T02 w/1");
        logic.execute("update 1 n/Alicia p/@alicia_new e/alicia.new@example.com");
        Person updated = model.getFilteredPersonList().getFirst();

        CommandResult result = logic.execute("mark 1 m/CS2103T w/1 s/absent");
        Person corrected = result.getSelectedPerson().orElseThrow();
        assertEquals("Attendance set: Alicia | CS2103T | Week 1 | Absent.", result.getFeedbackToUser());
        assertEquals("Alicia", corrected.getName().fullName);
        assertEquals("@alicia_new", corrected.getTelegramHandle().orElseThrow().value);
        assertEquals("alicia.new@example.com", corrected.getEmail().value);
        assertEquals(updated.withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("1"),
                AttendanceStatus.ABSENT)), corrected);
        assertEquals(3, corrected.getAttendance().size());
        assertEquals(List.of(corrected), model.getFilteredPersonList());
        List<Person> expected = List.of(corrected, other);
        assertEquals(expected, model.getAddressBook().getPersonList());
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(expected, restarted.getAddressBook().getPersonList());
        assertEquals(AttendanceStatus.ABSENT,
                restarted.getAddressBook().getPersonList().getFirst()
                        .getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
    }

    @Test
    public void mark_invalidInputs_doNotSaveOrMutate() {
        assertThrows(CommandException.class, "Student Bob is not enrolled in MA2001.", () ->
                logic.execute("mark 2 m/MA2001 w/1 s/present"));
        assertThrows(CommandException.class, MarkCommand.INVALID_INDEX, () ->
                logic.execute("mark 2147483647 m/CS2103T w/1 s/present"));
        assertThrows(ParseException.class, () -> logic.execute("mark 1 m/CS2103T w/54 s/present"));
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
        assertEquals(0, storage.saves);
    }

    @Test
    public void otherCommands_preserveAttendanceAndDeletionRemovesIt() throws Exception {
        logic.execute("mark 1 m/CS2103T w/1 s/present");
        Person recorded = model.getFilteredPersonList().getFirst();
        logic.execute("list");
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
        logic.execute("help");
        assertThrows(CommandException.class, EditCommand.MESSAGE_STUDENT_EDIT_UNSUPPORTED, () ->
                logic.execute("edit 1 n/Alicia"));
        assertEquals(List.of(recorded), model.getFilteredPersonList());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(recorded, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        logic.execute("delete 1");
        assertEquals(List.of(BOB), storage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("add n/Alice p/@alice_1 e/alice@example.com c/CS2103T:T04");
        assertTrue(model.getFilteredPersonList().getLast().getAttendance().isEmpty());
    }

    private static class CountingStorage extends JsonAddressBookStorage {
        private int saves;
        private boolean fail;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saves++;
            if (fail) {
                throw new IOException("Simulated full disk or permission failure");
            }
            super.saveAddressBook(addressBook);
        }
    }
}
