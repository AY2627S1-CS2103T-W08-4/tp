package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class StudentSelectionIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void select_fullFilteredAndRepeated_neverWritesOrChangesStudents() throws Exception {
        ModelManager model = new ModelManager();
        Person hidden = student("Hidden", "@hidden_one", "hidden@example.com", "MA2001:T01");
        Person shown = student("Shown", "@shown_one", "shown@example.com", "CS2103T:T04")
                .withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("3"), AttendanceStatus.PRESENT));
        model.addPerson(hidden);
        model.addPerson(shown);
        Path path = directory.resolve("students.json");
        new JsonAddressBookStorage(path).saveAddressBook(model.getAddressBook());
        String saved = Files.readString(path);
        AddressBook before = new AddressBook(model.getAddressBook());
        AtomicInteger changes = new AtomicInteger();
        model.getAddressBook().getPersonList().addListener((ListChangeListener<Person>) change ->
                changes.incrementAndGet());
        LogicManager logic = logic(model, path);
        assertEquals(shown, logic.execute("select 2").getSelectedPerson().orElseThrow());
        logic.execute("find c/CS2103T:T04 w/3");
        for (int i = 0; i < 2; i++) {
            CommandResult result = logic.execute("select 1");
            assertEquals(shown, result.getSelectedPerson().orElseThrow());
            assertFalse(result.isReplaceRosterContext());
            assertEquals(List.of(shown), model.getFilteredPersonList());
        }
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(path));
        assertEquals(0, changes.get());
    }

    @Test
    public void select_invalidSyntaxOrDisplayedBounds_preservesDataAndFilter() throws Exception {
        ModelManager model = new ModelManager();
        Person first = student("First", "@first_one", "first@example.com", "CS2103T:T04");
        Person hidden = student("Hidden", "@hidden_one", "hidden@example.com", "MA2001:T01");
        model.addPerson(hidden);
        model.addPerson(first);
        model.updateFilteredPersonList(first::equals);
        AddressBook before = new AddressBook(model.getAddressBook());
        Path path = directory.resolve("must-not-create.json");
        LogicManager logic = logic(model, path);
        assertThrows(CommandException.class, SelectCommand.MESSAGE_INVALID_INDEX, () -> logic.execute("select 2"));
        assertThrows(ParseException.class, () -> logic.execute("select 1 2"));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertFalse(Files.exists(path));
    }

    private LogicManager logic(ModelManager model, Path path) {
        JsonAddressBookStorage readOnly = new JsonAddressBookStorage(path) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new AssertionError("Selection must not attempt to save");
            }
        };
        return new LogicManager(model, new StorageManager(readOnly,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }
}
