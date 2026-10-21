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
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UpdateCommand;
import seedu.address.logic.commands.exceptions.CommandException;
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
import seedu.address.testutil.PersonBuilder;

public class StudentUpdateIntegrationTest {
    @TempDir
    public Path folder;
    private ModelManager model;
    private CountingStorage storage;
    private LogicManager logic;
    private Person alice;
    private Person bob;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        alice = student("Alice", "@alice_1", "alice@example.com", "CS2103T:T04", "MA2001:T01")
                .withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("1"), AttendanceStatus.PRESENT))
                .withAttendance(new AttendanceRecord("MA2001", new AttendanceWeek("53"), AttendanceStatus.ABSENT));
        bob = student("Bobby", "@bobby_1", "bob@example.com", "CS2103T:T05");
        model.addPerson(bob);
        model.addPerson(alice);
        storage = new CountingStorage(folder.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.writes = 0;
        logic = logic(model);
    }

    private LogicManager logic(ModelManager target) {
        return new LogicManager(target, new StorageManager(storage, new JsonUserPrefsStorage(folder.resolve("prefs"))));
    }

    @Test
    public void update_filteredIndex_preservesAttendanceMembershipOrderAndReloads() throws Exception {
        logic.execute("find c/CS2103T:T04 w/1");
        CommandResult result = logic.execute("update 1 n/ Alice  Tan p/@Alice_New e/ALICE.NEW@EXAMPLE.COM");
        Person updated = result.getSelectedPerson().orElseThrow();
        assertEquals("Student updated: Alice Tan.", result.getFeedbackToUser());
        assertEquals("@alice_new", updated.getTelegramHandle().orElseThrow().value);
        assertEquals("alice.new@example.com", updated.getEmail().value);
        assertEquals(alice.getAttendance(), updated.getAttendance());
        assertEquals(new ArrayList<>(alice.getMemberships()), new ArrayList<>(updated.getMemberships()));
        assertEquals(List.of(bob, updated), model.getAddressBook().getPersonList());
        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertFalse(result.isReplaceRosterContext());
        assertEquals(1, storage.writes);
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), restarted.getAddressBook());
        logic(restarted).execute("find c/CS2103T:T04 w/53");
        logic(restarted).execute("update 1 e/another@example.com");
        assertEquals(updated.getAttendance(), restarted.getFilteredPersonList().getFirst().getAttendance());
    }

    @Test
    public void update_singleFieldsAndNamesakes_preserveOmittedData() throws Exception {
        logic.execute("update 2 n/Bobby");
        Person renamed = model.getFilteredPersonList().get(1);
        assertEquals(bob.getName(), renamed.getName());
        assertEquals(alice.getTelegramHandle(), renamed.getTelegramHandle());
        assertEquals(alice.getEmail(), renamed.getEmail());
        logic.execute("update 2 p/@another_one");
        Person updated = model.getFilteredPersonList().get(1);
        assertEquals(renamed.getName(), updated.getName());
        assertEquals(renamed.getEmail(), updated.getEmail());
        assertEquals(alice.getAttendance(), updated.getAttendance());
    }

    @Test
    public void update_contactCollisionsOutsideFilter_emailTakesPrecedenceAndNothingChanges() throws Exception {
        Person third = student("Third", "@third_one", "third@example.com", "MA2001:T02");
        model.addPerson(third);
        logic.execute("find c/CS2103T:T04 w/1");
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_EMAIL, () ->
                logic.execute("update 1 e/BOB@EXAMPLE.COM p/@third_one"));
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_TELEGRAM, () ->
                logic.execute("update 1 p/@BOBBY_1"));
        model.addPerson(new PersonBuilder().withEmail("LEGACY@EXAMPLE.COM").build());
        logic.execute("find c/CS2103T:T04 w/1");
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_EMAIL, () ->
                logic.execute("update 1 e/legacy@example.com"));
        model.deletePerson(model.getAddressBook().getPersonList().getLast());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(0, storage.writes);
    }

    @Test
    public void update_noOp_selectsWithoutSavingEvenWhenStorageFails() throws Exception {
        logic.execute("find c/CS2103T:T04 w/1");
        storage.fail = true;
        CommandResult result = logic.execute("update 1 n/Alice p/@ALICE_1 e/ALICE@EXAMPLE.COM");
        assertEquals("Student unchanged: Alice.", result.getFeedbackToUser());
        assertEquals(alice, result.getSelectedPerson().orElseThrow());
        assertEquals(0, storage.writes);
    }

    @Test
    public void update_saveFailure_preservesStateAndAllowsRetry() throws Exception {
        logic.execute("find c/CS2103T:T04 w/1");
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        List<String> changes = new ArrayList<>();
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> changes.add("changed"));
        storage.fail = true;
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute("update 1 n/Changed"));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertTrue(changes.isEmpty());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        storage.fail = false;
        logic.execute("update 1 n/Changed");
        assertEquals("Changed", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(alice.getAttendance(), model.getFilteredPersonList().getFirst().getAttendance());
    }

    @Test
    public void update_invalidTarget_rejectedWithoutSaving() throws Exception {
        assertThrows(CommandException.class, UpdateCommand.INVALID_INDEX, () -> logic.execute("update 3 n/Changed"));
        model.addPerson(new PersonBuilder().build());
        assertThrows(CommandException.class, UpdateCommand.MESSAGE_LEGACY, () -> logic.execute("update 3 n/Changed"));
        logic.execute("find c/MA2001:T09 w/1");
        assertThrows(CommandException.class, UpdateCommand.INVALID_INDEX, () -> logic.execute("update 1 n/Changed"));
        assertEquals(0, storage.writes);
    }

    private static class CountingStorage extends JsonAddressBookStorage {
        private int writes;
        private boolean fail;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            writes++;
            if (fail) {
                throw new IOException("Injected disk failure");
            }
            super.saveAddressBook(data);
        }
    }
}
