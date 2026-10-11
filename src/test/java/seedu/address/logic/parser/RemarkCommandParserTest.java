package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_remarkText_returnsCommand() {
        assertParseSuccess(parser, " 1 r/Likes swimming ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")));
    }

    @Test
    public void parse_emptyOrMissingRemark_returnsRemovalCommand() {
        RemarkCommand removal = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", removal);
        assertParseSuccess(parser, "1", removal);
    }

    @Test
    public void parse_invalidIndex_showsUsage() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String input : List.of("", "r/Note", "0 r/Note", "-1 r/Note", "abc r/Note")) {
            assertParseFailure(parser, input, message);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void addressBookParser_dispatchesRemarkCommand() throws Exception {
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Note")),
                new AddressBookParser().parseCommand("remark 1 r/Note"));
    }
}
