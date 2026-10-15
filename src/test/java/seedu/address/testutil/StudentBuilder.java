package seedu.address.testutil;

import java.util.LinkedHashSet;
import java.util.List;

import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;

/** Complete student fixtures, separate from the retained legacy contact fixtures. */
public class StudentBuilder {
    public static final String COMMAND = "add n/Anne-Marie Tan p/@Anne_Tan e/Anne@Example.com c/cs2103t:t04";

    /** Builds a student with the given contact fields and memberships. */
    public static Person student(String name, String telegram, String email, String... memberships) {
        LinkedHashSet<ClassMembership> classes = new LinkedHashSet<>();
        for (String membership : List.of(memberships)) {
            classes.add(new ClassMembership(membership));
        }
        return new Person(Name.forStudent(name), new TelegramHandle(telegram), Email.forStudent(email), classes);
    }

    public static Person typicalStudent() {
        return student("Anne-Marie Tan", "@anne_tan", "anne@example.com", "CS2103T:T04");
    }
}
