package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a clear command without accepting arguments or control whitespace.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses the raw arguments, allowing only ordinary spaces (U+0020).
     *
     * @throws ParseException If any extra argument or other whitespace is present.
     */
    @Override
    public ClearCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.matches(" *")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
        }
        return new ClearCommand();
    }
}
