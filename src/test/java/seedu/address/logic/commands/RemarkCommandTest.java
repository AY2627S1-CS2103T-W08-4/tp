package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for RemarkCommand.
 */
public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemark_success() {
        assertRemarkSuccess("Likes swimming");
    }

    @Test
    public void execute_replaceRemark_success() {
        setFirstRemark("Old remark");
        assertRemarkSuccess("New remark");
    }

    @Test
    public void execute_removeRemark_success() {
        setFirstRemark("Old remark");
        assertRemarkSuccess("");
    }

    @Test
    public void execute_removeEmptyRemark_success() {
        assertRemarkSuccess("");
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        // Display the second person at index 1 to verify use of the displayed list.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess("Filtered person remark");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(invalidIndex, new Remark("Some remark")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Some remark")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    private void setFirstRemark(String text) {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withRemark(text).build());
    }

    private void assertRemarkSuccess(String text) {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withRemark(text).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = text.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(text)), model,
                String.format(message, Messages.format(edited)), expectedModel);
    }

    @Test
    public void equals() {
        final RemarkCommand standardCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));

        // same values -> returns true
        RemarkCommand commandWithSameValues = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY))));

        // different remark -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_BOB))));
    }
}
