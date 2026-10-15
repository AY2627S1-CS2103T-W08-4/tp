package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;
import static seedu.address.testutil.StudentBuilder.typicalStudent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.StudentBuilder;

public class StudentAddIntegrationTest {
    @TempDir
    public Path folder;

    private LogicManager logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage, new JsonUserPrefsStorage(folder.resolve("prefs"))));
    }

    @Test
    public void add_filteredRoster_appendsSelectsAndReloadsCompleteStudent() throws Exception {
        ModelManager model = new ModelManager();
        Person legacy = new PersonBuilder().build();
        model.addPerson(legacy);
        model.updateFilteredPersonList(person -> false);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("students.json"));
        CommandResult result = logic(model, storage).execute(StudentBuilder.COMMAND + " c/ma2001:t05");
        Person expected = student("Anne-Marie Tan", "@anne_tan", "anne@example.com", "CS2103T:T04", "MA2001:T05");
        assertEquals("New student added: Anne-Marie Tan.", result.getFeedbackToUser());
        assertEquals(expected, result.getSelectedPerson().orElseThrow());
        assertEquals(List.of(legacy, expected), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertFalse(storage.readAddressBook().orElseThrow().getPersonList().getFirst().isStudent());
    }

    @Test
    public void add_namesakesAllowed_contactsRejectedEmailFirstAcrossDifferentStudents() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(typicalStudent());
        model.addPerson(student("Anne-Marie Tan", "@other_one", "other@example.com", "MA2001:T01"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("students.json"));
        LogicManager logic = logic(model, storage);
        logic.execute("add n/Anne-Marie Tan p/@third_one e/third@example.com c/MA2001:T01");
        assertEquals(3, model.getFilteredPersonList().size());
        model.updateFilteredPersonList(person -> person.getEmail().value.equals("third@example.com"));
        String original = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_EMAIL, () ->
                 logic.execute("add n/Someone p/@OTHER_ONE e/ANNE@EXAMPLE.COM c/MA2001:T01"));
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_TELEGRAM, () ->
                 logic.execute("add n/Someone p/@ANNE_TAN e/new@example.com c/MA2001:T01"));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(original, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void add_legacyEmailCollision_rejectedIgnoringCase() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withEmail("ANNE@EXAMPLE.COM").build());
        LogicManager logic = logic(model, new JsonAddressBookStorage(folder.resolve("students.json")));
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_EMAIL, () ->
                 logic.execute(StudentBuilder.COMMAND));
    }

    @Test
    public void add_failedSave_leavesDataPredicateAndObservableListUntouched() throws Exception {
        ModelManager model = new ModelManager();
        Person first = student("First", "@first_one", "first@example.com", "MA2001:T01");
        model.addPerson(first);
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("First"));
        Path path = folder.resolve("students.json");
        JsonAddressBookStorage initialStorage = new JsonAddressBookStorage(path);
        initialStorage.saveAddressBook(model.getAddressBook());
        String original = Files.readString(path);
        List<String> changes = new ArrayList<>();
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> changes.add("changed"));
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(path) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidate) throws IOException {
                assertEquals(2, candidate.getPersonList().size());
                throw new IOException("Disk full");
            }
        };
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () ->
                 logic(model, failingStorage).execute(StudentBuilder.COMMAND));
        assertEquals(List.of(first), model.getAddressBook().getPersonList());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertTrue(changes.isEmpty());
        assertEquals(original, Files.readString(path));
        model.setPerson(first, student("Changed", "@first_one", "first@example.com", "MA2001:T01"));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void legacyEdit_studentTargetRejectedWithoutLosingFields() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(typicalStudent());
        LogicManager logic = logic(model, new JsonAddressBookStorage(folder.resolve("students.json")));
        assertThrows(CommandException.class, EditCommand.MESSAGE_STUDENT_EDIT_UNSUPPORTED, () ->
                 logic.execute("edit 1 n/Changed"));
        assertEquals(List.of(typicalStudent()), model.getAddressBook().getPersonList());
    }
}
