package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        String expectedMessage = ListCommand.getSuccessMessage(expectedModel.getFilteredPersonList().size());
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        String expectedMessage = ListCommand.getSuccessMessage(expectedModel.getFilteredPersonList().size());
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsZeroStudents() {
        Model emptyModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel, "0 students listed!", new ModelManager());
    }

    @Test
    public void execute_oneStudent_usesSingularMessage() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model oneStudentModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedOneStudentModel = new ModelManager(addressBook, new UserPrefs());
        assertCommandSuccess(new ListCommand(), oneStudentModel, "1 student listed!", expectedOneStudentModel);
    }

    @Test
    public void getSuccessMessage() {
        assertEquals("0 students listed!", ListCommand.getSuccessMessage(0));
        assertEquals("1 student listed!", ListCommand.getSuccessMessage(1));
        assertEquals("2 students listed!", ListCommand.getSuccessMessage(2));
        assertEquals("7 students listed!", ListCommand.getSuccessMessage(7));
    }
}
