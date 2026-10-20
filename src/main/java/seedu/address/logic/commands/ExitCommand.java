package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Terminates the program.
 */
public class ExitCommand extends Command {

    public static final String COMMAND_WORD = "exit";

    public static final String MESSAGE_USAGE = "Format: exit";

    public static final String MESSAGE_EXIT_ACKNOWLEDGEMENT = "Exiting TutorTrack as requested ...";

    public static final String MESSAGE_SAVE_FAILURE = "Unable to save changes. TutorTrack will remain open. "
            + "Check available disk space and file permissions, then try again.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
    }

}
