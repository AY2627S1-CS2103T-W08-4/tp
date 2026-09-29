package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises remarks through the command parser, model, editing and persistent storage.
 */
public class RemarkCommandTest {
    @TempDir
    public Path temporaryFolder;

    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addReplaceAndClear_updatesOnlyRemark() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        parser.parseCommand("remark 1 r/Likes swimming").execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Likes swimming").build(),
                model.getFilteredPersonList().get(0));
        parser.parseCommand("remark 1 r/Likes baseball").execute(model);
        assertEquals(new Remark("Likes baseball"), model.getFilteredPersonList().get(0).getRemark());
        parser.parseCommand("remark 1 r/").execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
        parser.parseCommand("remark 1 r/Another note").execute(model);
        parser.parseCommand("remark 1").execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Person secondPerson = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("remark 1 r/Filtered contact").execute(model);
        assertEquals(new PersonBuilder(secondPerson).withRemark("Filtered contact").build(),
                model.getAddressBook().getPersonList().get(1));
        assertTrue(model.getFilteredPersonList().size() > 1);
    }

    @Test
    public void execute_invalidIndex_failsWithoutChangingData() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Command command = parser.parseCommand("remark 999 r/Note");
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void parse_invalidInputs_rejected() {
        for (String input : new String[]{"remark", "remark 0 r/note", "remark -1 r/note",
            "remark abc r/note", "remark 1 r/first r/second"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void execute_editAndStorageRoundTrip_preservesRemark() throws Exception {
        parser.parseCommand("remark 1 r/Follow up next week").execute(model);
        parser.parseCommand("edit 1 p/91234567").execute(model);
        assertEquals(new Remark("Follow up next week"), model.getFilteredPersonList().get(0).getRemark());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
