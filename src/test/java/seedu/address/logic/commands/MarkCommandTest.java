package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.AttendanceTestData.ALICE;
import static seedu.address.testutil.AttendanceTestData.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;

public class MarkCommandTest {
    @Test
    public void execute_laterUnenrolledTarget_doesNotApplyEarlierValidDecision() {
        ModelManager model = roster();
        MarkCommand command = command(List.of(1, 2), "CS1101S");
        assertThrows(CommandException.class, "Student Bob is not enrolled in CS1101S.", () -> command.execute(model));
        assertEquals(List.of(ALICE, BOB), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidBounds_precedesEnrolmentRegardlessOfInputOrder() {
        ModelManager model = roster();
        for (List<Integer> indices : List.of(List.of(2, 3), List.of(3, 2))) {
            assertThrows(CommandException.class, MarkCommand.INVALID_INDEX, () ->
                    command(indices, "CS1101S").execute(model));
        }
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
    }

    @Test
    public void execute_unenrolledTargets_reportsFirstInInputOrder() {
        ModelManager model = roster();
        assertThrows(CommandException.class, "Student Bob is not enrolled in MA2001.", () ->
                command(List.of(2, 1), "MA2001").execute(model));
        assertThrows(CommandException.class, "Student Alice is not enrolled in MA2001.", () ->
                command(List.of(1, 2), "MA2001").execute(model));
        assertEquals(List.of(ALICE, BOB), model.getFilteredPersonList());
    }

    @Test
    public void execute_replacementsLeaveFilter_resolvesAllTargetsBeforeChangingRows() throws Exception {
        ModelManager model = roster();
        AttendanceWeek week = new AttendanceWeek("1");
        model.updateFilteredPersonList(person -> person.getAttendanceStatus("CS2103T", week)
                == AttendanceStatus.UNRECORDED);
        CommandResult result = command(List.of(2, 1, 2), "CS2103T").execute(model);
        assertEquals("Attendance set: CS2103T | Week 1 | Present | 2 changed, 0 unchanged.",
                result.getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(ALICE.withAttendance(decision("CS2103T")), BOB.withAttendance(decision("CS2103T"))),
                model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook().getPersonList().get(1), result.getSelectedPerson().orElseThrow());
    }

    @Test
    public void execute_duplicateSingleTarget_retainsMessagesAndUnchangedObject() throws Exception {
        ModelManager model = roster();
        MarkCommand command = command(List.of(2, 2), "CS2103T");
        assertEquals("Attendance set: Bob | CS2103T | Week 1 | Present.", command.execute(model).getFeedbackToUser());
        var recorded = model.getFilteredPersonList().get(1);
        CommandResult repeated = command.execute(model);
        assertEquals("Attendance unchanged: Bob | CS2103T | Week 1 is already Present.", repeated.getFeedbackToUser());
        assertSame(recorded, repeated.getSelectedPerson().orElseThrow());
        assertEquals(ALICE, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void equals_deduplicatesButPreservesSelectionOrderAndDecision() {
        MarkCommand command = command(List.of(2, 1, 2), "CS2103T");
        assertEquals(command, command(List.of(2, 1), "CS2103T"));
        assertEquals(command.hashCode(), command(List.of(2, 1), "CS2103T").hashCode());
        assertNotEquals(command, command(List.of(1, 2), "CS2103T"));
        assertNotEquals(command, command(List.of(2, 1), "CS1101S"));
        assertNotEquals(command, null);
        assertThrows(IllegalArgumentException.class, () -> command(List.of(), "CS2103T"));
    }

    private ModelManager roster() {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        return model;
    }

    private MarkCommand command(List<Integer> indices, String course) {
        return new MarkCommand(indices.stream().map(Index::fromOneBased).toList(), decision(course));
    }

    private AttendanceRecord decision(String course) {
        return new AttendanceRecord(course, new AttendanceWeek("1"), AttendanceStatus.PRESENT);
    }
}
