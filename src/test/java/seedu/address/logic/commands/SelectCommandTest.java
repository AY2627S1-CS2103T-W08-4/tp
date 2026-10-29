package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SelectCommandTest {
    @Test
    public void execute_filteredIndex_returnsDisplayedPersonWithoutChangingModel() throws Exception {
        ModelManager model = new ModelManager();
        Person hidden = student("Hidden", "@hidden_one", "hidden@example.com", "MA2001:T01");
        Person shown = student("Shown", "@shown_one", "shown@example.com", "CS2103T:T04");
        model.addPerson(hidden);
        model.addPerson(shown);
        model.updateFilteredPersonList(shown::equals);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandResult result = new SelectCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(shown, result.getSelectedPerson().orElseThrow());
        assertEquals("Selected: Shown.", result.getFeedbackToUser());
        assertFalse(result.isClearSelection());
        assertFalse(result.isReplaceRosterContext());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(before, model.getAddressBook());
        assertEquals(java.util.List.of(shown), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyOrOutOfBounds_throwsWithoutChangingRoster() {
        ModelManager model = new ModelManager();
        SelectCommand command = new SelectCommand(Index.fromOneBased(1));
        assertThrows(CommandException.class, SelectCommand.MESSAGE_INVALID_INDEX, () -> command.execute(model));
        model.addPerson(new PersonBuilder().build());
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, SelectCommand.MESSAGE_INVALID_INDEX, () ->
                new SelectCommand(Index.fromOneBased(2)).execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_legacyContact_canBeSelected() throws Exception {
        ModelManager model = new ModelManager();
        Person legacy = new PersonBuilder().build();
        model.addPerson(legacy);
        assertEquals(legacy, new SelectCommand(Index.fromOneBased(1)).execute(model).getSelectedPerson().orElseThrow());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new SelectCommand(null));
        assertThrows(NullPointerException.class, () -> new SelectCommand(Index.fromOneBased(1)).execute(null));
    }

    @Test
    public void equalsAndHashCode() {
        SelectCommand first = new SelectCommand(Index.fromOneBased(1));
        SelectCommand same = new SelectCommand(Index.fromOneBased(1));
        assertTrue(first.equals(first));
        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(new SelectCommand(Index.fromOneBased(2))));
        assertFalse(first.equals(null));
        assertFalse(first.equals("select"));
    }
}
