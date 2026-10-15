package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    private static final String ONLY_SPACES_REGEX = " *";

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * Only ordinary spaces (U+0020) are allowed; any other input, including tabs and line breaks, is rejected.
     * @throws ParseException if the user input contains extra arguments
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.matches(ONLY_SPACES_REGEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }
        return new ListCommand();
    }
}
