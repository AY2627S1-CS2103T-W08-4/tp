package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;
import static seedu.address.testutil.StudentBuilder.typicalStudent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StudentPersonTest {
    @Test
    public void memberships_immutableDefensiveCopyWithOneGroupPerCourse() {
        Set<ClassMembership> classes = new LinkedHashSet<>(List.of(new ClassMembership("CS2103T:T04")));
        Person person = new Person(Name.forStudent("Anne"), new TelegramHandle("@anne_tan"),
                Email.forStudent("anne@example.com"), classes);
        classes.clear();
        assertEquals(Set.of(new ClassMembership("CS2103T:T04")), person.getMemberships());
        assertThrows(UnsupportedOperationException.class, () -> person.getMemberships().clear());
        assertThrows(IllegalArgumentException.class, () -> student("Anne", "@anne_tan", "anne@example.com"));
        assertThrows(IllegalArgumentException.class, () -> student("Anne", "@anne_tan", "anne@example.com",
                "CS2103T:T04", "CS2103T:T05"));
    }

    @Test
    public void identity_namesakesAllowedAndContactCollisionsSymmetric() {
        Person first = typicalStudent();
        Person namesake = student("Anne-Marie Tan", "@other_one", "other@example.com", "CS2103T:T04");
        assertFalse(first.isSamePerson(namesake));
        assertFalse(namesake.isSamePerson(first));
        Person telegramCollision = student("Another", "@ANNE_TAN", "other@example.com", "CS2103T:T04");
        assertTrue(first.isSamePerson(telegramCollision));
        Person legacyCollision = new PersonBuilder().withEmail("ANNE@EXAMPLE.COM").build();
        assertTrue(first.isSamePerson(legacyCollision));
        assertTrue(legacyCollision.isSamePerson(first));
        assertEquals(first, typicalStudent());
        assertEquals(first.hashCode(), typicalStudent().hashCode());
        assertFalse(first.equals(namesake));
        assertFalse(first.equals(student("Anne-Marie Tan", "@anne_tan", "anne@example.com", "MA2001:T01")));
    }
}
