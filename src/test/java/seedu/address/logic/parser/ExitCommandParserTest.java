package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.model.ModelManager;

public class ExitCommandParserTest {

    private final ExitCommandParser parser = new ExitCommandParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_emptyArguments_success() throws Exception {
        CommandResult expectedResult = new CommandResult(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
        assertEquals(expectedResult, parser.parse("").execute(new ModelManager()));
    }

    @Test
    public void parse_whitespaceArguments_success() throws Exception {
        CommandResult expectedResult = new CommandResult(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
        assertEquals(expectedResult, parser.parse("  \t  ").execute(new ModelManager()));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 3", expectedMessage);
        assertParseFailure(parser, " anything", expectedMessage);
        assertParseFailure(parser, " n/Alice", expectedMessage);
        assertParseFailure(parser, "  3  ", expectedMessage);
    }
}
