package seedu.address.logic.parser;

import static seedu.address.commons.util.StringUtil.trimOrdinarySpaces;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses exactly one displayed index without accepting control whitespace. */
public class SelectCommandParser implements Parser<SelectCommand> {
    @Override
    public SelectCommand parse(String args) throws ParseException {
        String text = trimOrdinarySpaces(args);
        try {
            if (!text.matches("[1-9][0-9]*")) {
                throw new NumberFormatException();
            }
            return new SelectCommand(Index.fromOneBased(Integer.parseInt(text)));
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE), e);
        }
    }
}
