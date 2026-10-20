package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.MarkCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MvpRosterIntegrationTest {
    @TempDir
    public Path directory;
    private ModelManager model;
    private LogicManager logic;
    private FailingStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new FailingStorage(directory.resolve("students.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/Alice p/@alice_1 e/alice@example.com c/CS2103T:T04 c/MA2001:T01");
        logic.execute("add n/Bobby p/@bobby_1 e/bobby@example.com c/CS1101S:T04");
        logic.execute("add n/Carol p/@carol_1 e/carol@example.com c/CS2103T:T04");
        logic.execute("add n/David p/@david_1 e/david@example.com c/CS2103T:T05");
    }

    @Test
    public void batchDelete_filteredSnapshot_removesOnlyTargetsAndPersistsAttendanceRemoval() throws Exception {
        logic.execute("mark 3 m/CS2103T w/3 s/present");
        logic.execute("find c/CS2103T:T04 w/3");
        assertEquals(List.of("Alice", "Carol"), names());
        assertEquals("Deleted student(s): Carol | Alice", logic.execute("delete 2 1 2").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of("Bobby", "David"), storage.readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        logic.execute("list");
        logic.execute("add n/Carol p/@carol_1 e/carol@example.com c/CS2103T:T04");
        assertTrue(model.getFilteredPersonList().getLast().getAttendance().isEmpty());
    }

    @Test
    public void batchDelete_invalidLaterTarget_leavesAllRecordsAndDiskUnchanged() throws Exception {
        logic.execute("find c/CS2103T:T04 w/3");
        String disk = Files.readString(storage.getAddressBookFilePath());
        List<Person> before = List.copyOf(model.getFilteredPersonList());
        assertThrows(CommandException.class, DeleteCommand.MESSAGE_INVALID_INDEX, () -> logic.execute("delete 1 3"));
        for (String invalid : List.of("0", "01", "+1", "-1", "1.0", "2147483648", "١", "1\t2")) {
            assertThrows(ParseException.class, MarkCommandParser.INDEX_CONSTRAINTS, () ->
                    logic.execute("delete " + invalid));
        }
        assertEquals(before, model.getFilteredPersonList());
        assertEquals(disk, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void delete_saveFailure_preservesStateAndAllowsRetry() throws Exception {
        logic.execute("find c/CS2103T:T04 w/3");
        List<Person> before = List.copyOf(model.getFilteredPersonList());
        String disk = Files.readString(storage.getAddressBookFilePath());
        storage.fail = true;
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute("delete 1 2"));
        assertEquals(before, model.getFilteredPersonList());
        assertEquals(4, model.getAddressBook().getPersonList().size());
        assertEquals(disk, Files.readString(storage.getAddressBookFilePath()));
        storage.fail = false;
        logic.execute("delete 1 2");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void queries_readOnlyAndInvalidInputPreservesView() throws Exception {
        storage.fail = true;
        int saves = storage.saves;
        logic.execute("find w/53 c/cs2103t:t04");
        assertEquals(List.of("Alice", "Carol"), names());
        assertThrows(ParseException.class, () -> logic.execute("find c/CS2103T:T05 w/0"));
        assertEquals(List.of("Alice", "Carol"), names());
        logic.execute("find c/MA2001:T09 w/3");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals("4 students listed!", logic.execute("list").getFeedbackToUser());
        assertEquals(saves, storage.saves);
    }

    @Test
    public void classAttendance_correctClearOtherWeekAndRestart_keepIndependentRecords() throws Exception {
        logic.execute("find c/CS2103T:T04 w/3");
        logic.execute("mark 2 m/CS2103T w/3 s/present");
        logic.execute("mark 2 m/CS2103T w/4 s/present");
        logic.execute("mark 2 m/CS2103T w/3 s/absent");
        logic.execute("mark 1 m/MA2001 w/3 s/absent");
        assertEquals(List.of("Alice", "Carol"), names());
        logic.execute("mark 2 m/CS2103T w/3 s/unrecorded");
        assertTrue(logic.execute("exit").isExit());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person carol = restored.getAddressBook().getPersonList().get(2);
        assertEquals(AttendanceStatus.UNRECORDED, carol.getAttendanceStatus("CS2103T", new AttendanceWeek("3")));
        assertEquals(AttendanceStatus.PRESENT, carol.getAttendanceStatus("CS2103T", new AttendanceWeek("4")));
        assertFalse(restored.getAddressBook().getPersonList().getFirst().getAttendance().isEmpty());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private boolean fail;
        private int saves;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            if (fail) {
                throw new IOException("Injected save failure");
            }
            super.saveAddressBook(data);
        }
    }
}
