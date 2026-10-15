package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.Person;

/** Records, corrects or clears one displayed student's attendance. */
public class MarkCommand extends Command {
    public static final String COMMAND_WORD = "mark";
    public static final String MESSAGE_USAGE = "Format: mark INDEX m/COURSE w/WEEK s/STATUS";
    public static final String INVALID_INDEX = "The student index provided is invalid.";

    private final Index index;
    private final AttendanceRecord decision;

    /** Targets a row in the current display with a validated attendance decision. */
    public MarkCommand(Index index, AttendanceRecord decision) {
        requireAllNonNull(index, decision);
        this.index = index;
        this.decision = decision;
    }

    /** Resolves the index against the current display, before any update occurs. */
    public Person getTarget(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(INVALID_INDEX);
        }
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person target = getTarget(model);
        if (target.getMemberships().stream().noneMatch(membership -> membership.courseCode.equals(decision.course))) {
            throw new CommandException("Student " + target.getName() + " is not enrolled in " + decision.course + ".");
        }
        AttendanceStatus previous = target.getAttendanceStatus(decision.course, decision.week);
        String context = target.getName() + " | " + decision.course + " | Week " + decision.week;
        if (previous == decision.status) {
            return new CommandResult("Attendance unchanged: " + context + " is already "
                    + decision.status + ".", target);
        }
        Person updated = target.withAttendance(decision);
        model.setPerson(target, updated);
        return new CommandResult("Attendance set: " + context + " | " + decision.status + ".", updated);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MarkCommand command && index.equals(command.index) && decision.equals(command.decision);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(index, decision);
    }
}
