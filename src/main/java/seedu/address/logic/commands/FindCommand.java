package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;

/**
 * Lists students belonging to an exact class for a chosen teaching week.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = "Format: find c/COURSE:GROUP w/WEEK";

    private final ClassMembership membership;
    private final AttendanceWeek week;

    /** Creates an exact normalized class/week query. */
    public FindCommand(ClassMembership membership, AttendanceWeek week) {
        this.membership = requireNonNull(membership);
        this.week = requireNonNull(week);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> person.isStudent() && person.getMemberships().contains(membership));
        return CommandResult.roster(ListCommand.getSuccessMessage(model.getFilteredPersonList().size()),
                membership, week);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return Objects.equals(membership, otherFindCommand.membership)
                && Objects.equals(week, otherFindCommand.week);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("membership", membership)
                .add("week", week)
                .toString();
    }
}
