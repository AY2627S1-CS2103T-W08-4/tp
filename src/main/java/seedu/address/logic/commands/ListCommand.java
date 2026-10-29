package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;

/**
 * Removes any filter and lists all students in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "Format: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "%1$d students listed!";

    public static final String MESSAGE_SUCCESS_SINGULAR = "1 student listed!";

    private static final Logger logger = LogsCenter.getLogger(ListCommand.class);

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int count = model.getFilteredPersonList().size();
        logger.fine("Cleared roster filter; listing all " + count + " saved entries");
        return CommandResult.roster(getSuccessMessage(count), null, null);
    }

    /**
     * Returns the success message for listing {@code count} students.
     */
    public static String getSuccessMessage(int count) {
        assert count >= 0;
        return count == 1 ? MESSAGE_SUCCESS_SINGULAR : String.format(MESSAGE_SUCCESS, count);
    }
}
