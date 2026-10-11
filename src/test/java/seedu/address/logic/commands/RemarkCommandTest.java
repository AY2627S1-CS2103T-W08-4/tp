package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
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

public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addAndOverwrite_preservesOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming"));
        CommandResult result = command.execute(model);
        Person expected = new PersonBuilder(original).withRemark("Likes swimming").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());

        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Prefers mornings")).execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Prefers mornings").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_emptyRemark_removesRemark() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Old note")).execute(model);
        CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")).execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(original)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndShowsAll() throws Exception {
        Person first = model.getFilteredPersonList().get(0);
        Person second = model.getFilteredPersonList().get(1);
        int count = model.getFilteredPersonList().size();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Selected student")).execute(model);
        assertEquals(count, model.getFilteredPersonList().size());
        assertEquals(first, model.getFilteredPersonList().get(0));
        assertEquals(new PersonBuilder(second).withRemark("Selected student").build(),
                model.getFilteredPersonList().get(1));
    }

    @Test
    public void execute_invalidIndex_doesNotChangeModel() {
        Index outside = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outside, new Remark("Note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void nullInputs_areRejected() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("Note")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Note"));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals_comparesIndexAndRemark() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Note"));
        assertTrue(command.equals(command));
        assertEquals(command, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Note")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Different"))));
    }
}
