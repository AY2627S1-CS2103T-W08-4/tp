package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.Person;

/** Records, corrects or clears attendance for students in the pre-command display. */
public class MarkCommand extends Command {
    public static final String COMMAND_WORD = "mark";
    public static final String MESSAGE_USAGE = "Format: mark INDEX [INDEX ...] m/COURSE w/WEEK s/STATUS";
    public static final String INVALID_INDEX = "The student index provided is invalid.";

    private final List<Index> indices;
    private final AttendanceRecord decision;

    /** Targets a row in the current display with a validated attendance decision. */
    public MarkCommand(Index index, AttendanceRecord decision) {
        this(List.of(index), decision);
    }

    /** Deduplicates targets in input order; one unique target retains single-student feedback. */
    public MarkCommand(List<Index> indices, AttendanceRecord decision) {
        requireAllNonNull(indices, decision);
        if (indices.isEmpty()) {
            throw new IllegalArgumentException("At least one index is required.");
        }
        this.indices = indices.stream().map(Index::getZeroBased).distinct().map(Index::fromZeroBased).toList();
        this.decision = decision;
    }

    /** Resolves every index against one snapshot, checking all bounds before enrolment. */
    public List<Person> getTargets(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        for (Index index : indices) {
            if (index.getZeroBased() >= displayed.size()) {
                throw new CommandException(INVALID_INDEX);
            }
        }
        return indices.stream().map(index -> displayed.get(index.getZeroBased())).toList();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> targets = getTargets(model);
        for (Person target : targets) {
            if (target.getMemberships().stream()
                    .noneMatch(membership -> membership.courseCode.equals(decision.course))) {
                throw new CommandException("Student " + target.getName()
                        + " is not enrolled in " + decision.course + ".");
            }
        }
        // Resolve and validate the whole batch before replacements can change a filtered list.
        List<Person> replacements = targets.stream().map(target ->
                target.getAttendanceStatus(decision.course, decision.week) == decision.status
                        ? target : target.withAttendance(decision)).toList();
        int changed = 0;
        for (int i = 0; i < targets.size(); i++) {
            if (!targets.get(i).equals(replacements.get(i))) {
                model.setPerson(targets.get(i), replacements.get(i));
                changed++;
            }
        }
        Person selected = replacements.getFirst();
        if (targets.size() > 1) {
            String action = changed == 0 ? "Attendance unchanged: " : "Attendance set: ";
            return new CommandResult(action + decision.course + " | Week " + decision.week + " | "
                    + decision.status + " | " + changed + " changed, " + (targets.size() - changed) + " unchanged.",
                    selected);
        }
        Person target = targets.getFirst();
        AttendanceStatus previous = target.getAttendanceStatus(decision.course, decision.week);
        String context = target.getName() + " | " + decision.course + " | Week " + decision.week;
        if (previous == decision.status) {
            return new CommandResult("Attendance unchanged: " + context + " is already "
                    + decision.status + ".", target);
        }
        return new CommandResult("Attendance set: " + context + " | " + decision.status + ".", selected);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MarkCommand command && indices.equals(command.indices)
                && decision.equals(command.decision);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(indices.stream().map(Index::getZeroBased).toList(), decision);
    }
}
