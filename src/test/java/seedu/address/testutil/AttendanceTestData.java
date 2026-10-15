package seedu.address.testutil;

import java.util.Set;

import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;

/** Complete students shared by attendance integration tests. */
public final class AttendanceTestData {
    public static final Person ALICE = new Person(Name.forStudent("Alice"), new TelegramHandle("@alice_1"),
            Email.forStudent("alice@example.com"),
            Set.of(new ClassMembership("CS2103T:T04"), new ClassMembership("CS1101S:T02")));
    public static final Person BOB = new Person(Name.forStudent("Bob"), new TelegramHandle("@bobby_1"),
            Email.forStudent("bob@example.com"), Set.of(new ClassMembership("CS2103T:T04")));

    private AttendanceTestData() {
    }
}
