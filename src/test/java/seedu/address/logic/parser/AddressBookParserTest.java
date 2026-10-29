package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

/**
 * Verifies command dispatch and rejection of invalid top-level input.
 */
public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = seedu.address.testutil.StudentBuilder.typicalStudent();
        AddCommand command = (AddCommand) parser.parseCommand(seedu.address.testutil.StudentBuilder.COMMAND);
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_addWithLeadingControlWhitespace_rejected() {
        for (String whitespace : new String[]{"\t", "\n", "\r"}) {
            assertThrows(ParseException.class, () ->
                    parser.parseCommand(whitespace + seedu.address.testutil.StudentBuilder.COMMAND));
        }
    }

    @Test
    public void parseCommand_markWithControlWhitespace_rejected() {
        String command = "mark 1 m/CS2103T w/1 s/present";
        for (String whitespace : new String[]{"\t", "\n", "\r"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(whitespace + command));
            assertThrows(ParseException.class, () -> parser.parseCommand(command + whitespace));
        }
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand("  clear  ") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_clearWithArguments_throwsParseException() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        for (String input : new String[]{"clear 3", "clear anything", "clear n/Alice", "clear\t",
            "\tclear", "clear\n", "clear \r", "  clear  1  "}) {
            assertThrows(ParseException.class, expected, () -> parser.parseCommand(input));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("clear\u00a0"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand("  " + ExitCommand.COMMAND_WORD + "  ") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_exitWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("exit 3"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("exit anything"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("exit n/Alice"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("  exit   3  "));
    }

    @Test
    public void parseCommand_find() throws Exception {
        FindCommand command = (FindCommand) parser.parseCommand("find c/CS2103T:T04 w/3");
        assertEquals(new FindCommand(new ClassMembership("CS2103T:T04"), new AttendanceWeek("3")), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand("  " + ListCommand.COMMAND_WORD + "  ") instanceof ListCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ListCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_listWithNonSpaceWhitespace_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        String[] inputs = {"list\t", "list \t", "\tlist", "list\r"};
        for (String input : inputs) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }

        // Non-ASCII spaces are not command-word separators, so they make the command word unknown
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("list\u00a0"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("\u2003list"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
