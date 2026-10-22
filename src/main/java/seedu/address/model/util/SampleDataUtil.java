package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with fictional first-run students.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(Name.forStudent("Alex Yeoh"), new TelegramHandle("@alex_yeoh"),
                Email.forStudent("alexyeoh@example.com"),
                Set.of(new ClassMembership("CS2103T:T04"), new ClassMembership("CS1101S:T02"))),
            new Person(Name.forStudent("Bernice Yu"), new TelegramHandle("@bernice_yu"),
                Email.forStudent("berniceyu@example.com"),
                Set.of(new ClassMembership("CS2103T:T04"))),
            new Person(Name.forStudent("Charlotte Oliveiro"), new TelegramHandle("@charlotte_oliveiro"),
                Email.forStudent("charlotte@example.com"),
                Set.of(new ClassMembership("CS2103T:T05"))),
            new Person(Name.forStudent("David Li"), new TelegramHandle("@david_li"),
                Email.forStudent("lidavid@example.com"),
                Set.of(new ClassMembership("CS1101S:T02"))),
            new Person(Name.forStudent("Irfan Ibrahim"), new TelegramHandle("@irfan_ibrahim"),
                Email.forStudent("irfan@example.com"),
                Set.of(new ClassMembership("CS1101S:T03"))),
            new Person(Name.forStudent("Roy Balakrishnan"), new TelegramHandle("@roy_balakrishnan"),
                Email.forStudent("royb@example.com"),
                Set.of(new ClassMembership("CS2103T:T05")))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
