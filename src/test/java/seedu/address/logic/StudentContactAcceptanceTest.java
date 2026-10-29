package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises contact contracts through the public command path and real JSON persistence. */
public class StudentContactAcceptanceTest {
    @TempDir
    public Path directory;
    private ModelManager model;
    private RecordingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(student("Alice", "@alice_1", "alice@example.com", "CS2103T:T04"));
        model.addPerson(student("Bobby", "@bobby_1", "bob@example.com", "MA2001:T01"));
        storage = new RecordingStorage(directory.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.writes = 0;
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void add_maximumContactLengths_roundTripWithoutTruncation() throws Exception {
        // Supplementary letters occupy two UTF-16 units; the name limit counts code points.
        String name = new String(Character.toChars(0x10400)).repeat(50);
        String telegram = "@" + "a".repeat(32);
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        logic.execute("add n/" + name + " p/" + telegram + " e/" + email + " c/AB1234:A00 c/ABCD9999Z:Z99");
        Person added = model.getFilteredPersonList().getLast();
        assertEquals(name, added.getName().fullName);
        assertEquals(telegram, added.getTelegramHandle().orElseThrow().value);
        assertEquals(email, added.getEmail().value);
        assertTrue(added.getAttendance().isEmpty());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void addAndUpdate_oneBeyondEachContactLimit_rejectWithoutWriting() throws Exception {
        // Vary exactly one field at a time, so another invalid field cannot mask a missing boundary check.
        String longEmail = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(62);
        String[] invalidFields = {"n/" + "a".repeat(51), "p/@" + "a".repeat(33),
            "e/" + "a".repeat(65) + "@example.com", "e/a@" + "b".repeat(64) + ".com", "e/" + longEmail};
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String field : invalidFields) {
            String name = field.startsWith("n/") ? field : "n/Carol";
            String telegram = field.startsWith("p/") ? field : "p/@carol_1";
            String email = field.startsWith("e/") ? field : "e/carol@example.com";
            assertThrows(ParseException.class, () ->
                    logic.execute("add " + name + " " + telegram + " " + email + " c/CS2103T:T04"));
            assertThrows(ParseException.class, () -> logic.execute("update 1 " + field));
        }
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(0, storage.writes);
    }

    @Test
    public void update_normalizedUnicodeAndTurkishLocale_noOpDoesNotSave() throws Exception {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            logic.execute("update 1 n/José p/@IRIS_ONE e/IRIS@EXAMPLE.COM");
            storage.writes = 0;
            storage.failure = new IOException("No write should be attempted for a normalized no-op");
            CommandResult result = logic.execute("update 1 n/ Jose\u0301  p/@iris_one e/iris@example.com");
            assertEquals("Student unchanged: José.", result.getFeedbackToUser());
            assertEquals("@iris_one", result.getSelectedPerson().orElseThrow().getTelegramHandle().orElseThrow().value);
            assertEquals(0, storage.writes);
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void update_releasesOldContacts_newStudentCanUseThemAfterRestart() throws Exception {
        // Identity is the current contact values, not a stale cache of a student's former contacts.
        logic.execute("update 1 p/@alice_new e/alice.new@example.com");
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager afterRestart = new LogicManager(restarted, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        afterRestart.execute("add n/Namesake p/@ALICE_1 e/ALICE@EXAMPLE.COM c/CS2103T:T04");
        assertEquals(3, restarted.getFilteredPersonList().size());
        assertEquals("alice.new@example.com", restarted.getFilteredPersonList().getFirst().getEmail().value);
        assertEquals("alice@example.com", restarted.getFilteredPersonList().getLast().getEmail().value);
        assertEquals(restarted.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void add_permissionFailure_preservesFilteredViewThenRetriesOnce() throws Exception {
        logic.execute("find c/MA2001:T01 w/53");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        String command = "add n/Carol p/@carol_1 e/carol@example.com c/CS2103T:T04";
        storage.failure = new AccessDeniedException(storage.getAddressBookFilePath().toString());
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute(command));
        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));

        // Retrying the identical input after permission recovery must add exactly one complete profile.
        storage.failure = null;
        logic.execute(command);
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void update_permissionFailure_preservesOtherStudentThenRetries() throws Exception {
        logic.execute("find c/MA2001:T01 w/53");
        Person hidden = model.getAddressBook().getPersonList().getFirst();
        AddressBook before = new AddressBook(model.getAddressBook());
        storage.failure = new AccessDeniedException(storage.getAddressBookFilePath().toString());
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () ->
                logic.execute("update 1 n/Robert p/@robert_1 e/robert@example.com"));
        assertEquals(before, model.getAddressBook());
        assertEquals(before, storage.readAddressBook().orElseThrow());
        storage.failure = null;
        logic.execute("update 1 n/Robert p/@robert_1 e/robert@example.com");
        assertEquals(hidden, model.getAddressBook().getPersonList().getFirst());
        assertEquals("Robert", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void relationshipMarkers_namesakesAllowedButContactCollisionsDoNotWrite() throws Exception {
        logic.execute("add n/Ravi s/o Kumar p/@ravi_one e/ravi@example.com c/CS2103T:T04");
        logic.execute("add n/Ravi s/o Kumar p/@ravi_two e/ravi.two@example.com c/MA2001:T01");
        assertEquals(4, model.getFilteredPersonList().size());
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        storage.writes = 0;
        for (String contacts : List.of("p/@unique_one e/RAVI@EXAMPLE.COM", "p/@RAVI_ONE e/unique@example.com")) {
            assertThrows(CommandException.class, () -> logic.execute(
                    "add n/Priya d/o Raman " + contacts + " c/CS2103T:T04"));
            assertThrows(CommandException.class, () -> logic.execute("update 1 n/Priya d/o Raman " + contacts));
        }
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(0, storage.writes);
    }

    @Test
    public void relationshipMarkers_normalizedNoOpDoesNotWrite() throws Exception {
        logic.execute("update 1 n/José S/O 王");
        storage.writes = 0;
        storage.failure = new IOException("Normalized no-op must not save");
        CommandResult result = logic.execute("update 1 n/ Jose\u0301   S/O   王 ");
        assertEquals("Student unchanged: José S/O 王.", result.getFeedbackToUser());
        assertEquals(0, storage.writes);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void relationshipMarkers_failedSavesPreserveRosterAndFilterThenRetry() throws Exception {
        logic.execute("find c/MA2001:T01 w/53");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        String add = "add n/Ravi s/o Kumar p/@ravi_one e/ravi@example.com c/CS2103T:T04";
        String update = "update 1 n/Priya d/o Raman";
        storage.failure = new AccessDeniedException(storage.getAddressBookFilePath().toString());
        for (String command : List.of(add, update)) {
            assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        storage.failure = null;
        storage.writes = 0;
        logic.execute(update);
        assertEquals("Priya d/o Raman", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute(add);
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals("Ravi s/o Kumar", model.getFilteredPersonList().getLast().getName().fullName);
        assertEquals(2, storage.writes);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private static class RecordingStorage extends JsonAddressBookStorage {
        private int writes;
        private IOException failure;

        RecordingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            writes++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(data);
        }
    }
}
