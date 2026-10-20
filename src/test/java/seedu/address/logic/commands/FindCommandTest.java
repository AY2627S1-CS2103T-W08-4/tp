package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;

public class FindCommandTest {
    @Test
    public void execute_exactClass_retainsOrderExcludesOtherCoursesAndGroups() {
        Model model = new ModelManager();
        Person alice = student("Alice", "CS2103T:T04");
        Person bob = student("Bobby", "CS1101S:T04");
        Person carol = student("Carol", "CS2103T:T05");
        Person david = student("David", "CS2103T:T04");
        List.of(alice, bob, carol, david).forEach(model::addPerson);
        CommandResult result = new FindCommand(new ClassMembership("cs2103t:t04"),
                new AttendanceWeek("3")).execute(model);
        assertEquals(List.of(alice, david), model.getFilteredPersonList());
        assertEquals("2 students listed!", result.getFeedbackToUser());
        assertTrue(result.isClearSelection());
        assertEquals(new AttendanceWeek("3"), result.getRosterWeek());
        new FindCommand(new ClassMembership("CS2103T:T04"), new AttendanceWeek("4")).execute(model);
        assertEquals(List.of(alice, david), model.getFilteredPersonList());
        new FindCommand(new ClassMembership("MA2001:T01"), new AttendanceWeek("4")).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void equals_includesClassAndWeek() {
        FindCommand first = new FindCommand(new ClassMembership("CS2103T:T04"), new AttendanceWeek("3"));
        assertEquals(first, new FindCommand(new ClassMembership("cs2103t:t04"), new AttendanceWeek("3")));
        assertNotEquals(first, new FindCommand(new ClassMembership("CS2103T:T04"), new AttendanceWeek("4")));
        assertNotEquals(first, new FindCommand(new ClassMembership("CS2103T:T05"), new AttendanceWeek("3")));
    }

    private Person student(String name, String membership) {
        return new Person(Name.forStudent(name), new TelegramHandle("@" + name.toLowerCase()),
                Email.forStudent(name + "@example.com"), Set.of(new ClassMembership(membership)));
    }
}
