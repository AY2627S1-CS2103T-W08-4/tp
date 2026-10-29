package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Selects a displayed row without changing or saving student data. */
public class SelectCommand extends Command {
    public static final String COMMAND_WORD = "select";
    public static final String MESSAGE_USAGE = "Format: select INDEX";
    public static final String MESSAGE_SUCCESS = "Selected: %1$s.";
    public static final String MESSAGE_INVALID_INDEX = "The student index provided is invalid.";

    private final Index index;

    public SelectCommand(Index index) {
        this.index = requireNonNull(index);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Person target = displayed.get(index.getZeroBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, target.getName()), target);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SelectCommand command && index.equals(command.index);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index.getZeroBased());
    }
}
