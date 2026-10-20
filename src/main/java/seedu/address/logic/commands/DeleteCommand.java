package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = "Format: delete INDEX [INDEX...]";
    public static final String MESSAGE_DELETE_STUDENT_SUCCESS = "Deleted student(s): %1$s";
    public static final String MESSAGE_INVALID_INDEX = "The student index provided is invalid.";

    private final List<Index> targetIndices;

    public DeleteCommand(Index targetIndex) {
        this(List.of(targetIndex));
    }

    /** Repeated indices identify the same pre-command student and are deleted once. */
    public DeleteCommand(List<Index> targetIndices) {
        if (targetIndices.isEmpty()) {
            throw new IllegalArgumentException("At least one index is required.");
        }
        this.targetIndices = targetIndices.stream().map(Index::getZeroBased).distinct()
                .map(Index::fromZeroBased).toList();
    }

    /** Resolves every target before any change, retaining input order. */
    public List<Person> getTargets(Model model) throws CommandException {
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        for (Index index : targetIndices) {
            if (index.getZeroBased() >= displayed.size()) {
                throw new CommandException(MESSAGE_INVALID_INDEX);
            }
        }
        return targetIndices.stream().map(index -> displayed.get(index.getZeroBased())).toList();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> targets = getTargets(model);
        targets.forEach(model::deletePerson);
        String names = targets.stream().map(person -> person.getName().fullName).collect(Collectors.joining(" | "));
        return CommandResult.deletion(String.format(MESSAGE_DELETE_STUDENT_SUCCESS, names));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndices.equals(otherDeleteCommand.targetIndices);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndices", targetIndices)
                .toString();
    }
}
