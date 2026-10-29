package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.UpdateCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT =
            Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)", Pattern.DOTALL);
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        // Identify the command separately; strict parsers receive the untouched argument text below.
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case SelectCommand.COMMAND_WORD -> new SelectCommandParser().parse(getRawArguments(userInput, commandWord));
            case UpdateCommand.COMMAND_WORD -> new UpdateCommandParser().parse(getRawArguments(userInput, commandWord));
            case MarkCommand.COMMAND_WORD -> new MarkCommandParser().parse(getRawArguments(userInput, commandWord));
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(getRawArguments(userInput, commandWord));
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(getRawArguments(userInput, commandWord));
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(getRawArguments(userInput, commandWord));
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(getRawArguments(userInput, commandWord));
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(getRawArguments(userInput, commandWord));
            case ExitCommand.COMMAND_WORD -> new ExitCommandParser().parse(getRawArguments(userInput, commandWord));
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

    /**
     * Returns the arguments of {@code userInput} after {@code commandWord}, removing only leading ordinary spaces
     * (U+0020) before the command word. Other surrounding whitespace is kept so that parsers which accept only
     * ordinary spaces can reject it. If other whitespace precedes the command word, the whole input is returned.
     */
    private static String getRawArguments(String userInput, String commandWord) {
        String input = userInput.replaceFirst("^ +", "");
        return input.startsWith(commandWord) ? input.substring(commandWord.length()) : input;
    }

}
