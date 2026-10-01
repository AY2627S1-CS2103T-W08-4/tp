package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Displays the number of persons currently visible in the address book.
 */
public class CountCommand extends Command {
    public static final String COMMAND_WORD = "count";
    public static final String MESSAGE_SUCCESS = "There are %d persons in the address book.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size()));
    }
}
