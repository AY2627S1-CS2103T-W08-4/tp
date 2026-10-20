package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;
    private final Person selectedPerson;
    private final boolean clearSelection;
    private final boolean replaceRosterContext;
    private final ClassMembership rosterClass;
    private final AttendanceWeek rosterWeek;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, false, false, null, null);
    }

    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Requests selection after a successful command. */
    public CommandResult(String feedbackToUser, Person selectedPerson) {
        this(feedbackToUser, false, false, requireNonNull(selectedPerson), false, false, null, null);
    }

    private CommandResult(String feedback, boolean help, boolean exit, Person selected,
            boolean clear, boolean replace, ClassMembership membership, AttendanceWeek week) {
        feedbackToUser = requireNonNull(feedback);
        showHelp = help;
        this.exit = exit;
        selectedPerson = selected;
        clearSelection = clear;
        replaceRosterContext = replace;
        rosterClass = membership;
        rosterWeek = week;
    }

    /** Returns a result that replaces the roster context and clears selection. */
    public static CommandResult roster(String feedback, ClassMembership membership, AttendanceWeek week) {
        if ((membership == null) != (week == null)) {
            throw new IllegalArgumentException("Class and week must be supplied together.");
        }
        return new CommandResult(feedback, false, false, null, true, true, membership, week);
    }

    /** Returns a result that clears selection while retaining the current roster context. */
    public static CommandResult deletion(String feedback) {
        return new CommandResult(feedback, false, false, null, true, false, null, null);
    }

    /** Returns a result for a saved addition to the full roster. */
    public static CommandResult addition(String feedback, Person person) {
        return new CommandResult(feedback, false, false, requireNonNull(person), false, true, null, null);
    }

    public boolean isClearSelection() {
        return clearSelection;
    }

    public boolean isReplaceRosterContext() {
        return replaceRosterContext;
    }

    public ClassMembership getRosterClass() {
        return rosterClass;
    }

    public AttendanceWeek getRosterWeek() {
        return rosterWeek;
    }

    public Optional<Person> getSelectedPerson() {
        return Optional.ofNullable(selectedPerson);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(selectedPerson, otherCommandResult.selectedPerson)
                && clearSelection == otherCommandResult.clearSelection
                && replaceRosterContext == otherCommandResult.replaceRosterContext
                && Objects.equals(rosterClass, otherCommandResult.rosterClass)
                && Objects.equals(rosterWeek, otherCommandResult.rosterWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedPerson, clearSelection,
                replaceRosterContext, rosterClass, rosterWeek);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .toString();
    }

}
