package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
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

/**
 * Verifies that clearing a roster cannot publish unsaved data loss.
 */
public class ClearIntegrationTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private RecordingStorage storage;
    private LogicManager logic;
    private Person alice;
    private Person bob;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        alice = student("Alice", "@alice_1", "alice@example.com", "CS2103T:T04")
                .withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("1"), AttendanceStatus.PRESENT));
        bob = student("Bobby", "@bobby_1", "bob@example.com", "CS2103T:T05");
        model.addPerson(alice);
        model.addPerson(bob);
        storage = new RecordingStorage(directory.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.saves = 0;
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("find c/CS2103T:T04 w/1");
    }

    @Test
    public void clear_filteredRoster_savesBeforeRemovingAllRecordsAndAttendance() throws Exception {
        CommandResult result = logic.execute("clear");
        assertEquals(ClearCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(alice, bob), storage.livePeopleAtSave);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(result.isClearSelection());
        assertFalse(result.isReplaceRosterContext());
        assertEquals(1, storage.saves);
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(restarted.getAddressBook().getPersonList().isEmpty());
        // Replacing data without resetting the predicate checks that clear retained the class filter.
        AddressBook replacement = new AddressBook();
        replacement.addPerson(bob);
        model.setAddressBook(replacement);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void clear_failedSave_preservesRecordsAttendanceViewAndDiskThenAllowsRetry() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        List<String> events = new ArrayList<>();
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> events.add("changed"));
        storage.fail = true;
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute("clear"));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertTrue(events.isEmpty());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        storage.fail = false;
        logic.execute("clear");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertEquals(2, storage.saves);
    }

    @Test
    public void clear_extraArguments_preserveModelViewAndDiskWithoutSaving() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String input : List.of("clear 1", "clear n/Alice", "clear anything")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
        }
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(0, storage.saves);
    }

    @Test
    public void clear_emptyRoster_persistsEmptyState() throws Exception {
        logic.execute("clear");
        storage.saves = 0;
        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear").getFeedbackToUser());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertEquals(1, storage.saves);
    }

    /**
     * Captures live state at the save boundary and injects a failure before writing.
     */
    private class RecordingStorage extends JsonAddressBookStorage {
        private int saves;
        private boolean fail;
        private List<Person> livePeopleAtSave;

        RecordingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            livePeopleAtSave = List.copyOf(model.getAddressBook().getPersonList());
            if (fail) {
                throw new IOException("Injected clear save failure");
            }
            super.saveAddressBook(data);
        }
    }
}
