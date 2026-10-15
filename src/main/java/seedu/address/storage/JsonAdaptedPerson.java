package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String telegram;
    private final List<String> memberships;
    private final List<JsonAdaptedAttendance> attendance;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("telegram") String telegram, @JsonProperty("memberships") List<String> memberships,
            @JsonProperty("attendance") List<JsonAdaptedAttendance> attendance) {
        this.attendance = attendance == null ? List.of() : new ArrayList<>(attendance);
        this.telegram = telegram;
        this.memberships = memberships == null ? null : new ArrayList<>(memberships);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Retains the student adapter constructor for records predating attendance. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            String telegram, List<String> memberships) {
        this(name, phone, email, address, tags, telegram, memberships, null);
    }

    /** Retains the legacy adapter constructor for old contacts. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        attendance = source.getAttendance().stream().map(JsonAdaptedAttendance::new).collect(Collectors.toList());
        name = source.getName().fullName;
        phone = source.isStudent() ? null : source.getPhone().value;
        email = source.getEmail().value;
        address = source.isStudent() ? null : source.getAddress().value;
        telegram = source.getTelegramHandle().map(TelegramHandle::toString).orElse(null);
        memberships = source.isStudent()
                ? source.getMemberships().stream().map(ClassMembership::toString).collect(Collectors.toList()) : null;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (telegram != null || memberships != null) {
            return toStudent();
        }
        if (!attendance.isEmpty()) {
            throw new IllegalValueException("Legacy contacts cannot contain attendance without course memberships.");
        }
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags);
    }

    /** Validates all new-schema fields; incomplete students never fall back to legacy records. */
    private Person toStudent() throws IllegalValueException {
        if (name == null || email == null || telegram == null || memberships == null || memberships.isEmpty()) {
            throw new IllegalValueException("Student name, Telegram, email and memberships are required.");
        }
        if (phone != null || address != null || !tags.isEmpty()) {
            throw new IllegalValueException("Student records cannot also contain legacy phone, address or tags.");
        }
        try {
            Set<ClassMembership> classes = new LinkedHashSet<>();
            Set<String> courses = new HashSet<>();
            for (String membership : memberships) {
                if (membership == null) {
                    throw new IllegalValueException(ClassMembership.MESSAGE_CONSTRAINTS);
                }
                ClassMembership parsed = new ClassMembership(membership);
                if (!courses.add(parsed.courseCode)) {
                    throw new IllegalValueException("Course " + parsed.courseCode
                            + " may be specified only once per student.");
                }
                classes.add(parsed);
            }
            Person student = new Person(Name.forStudent(name), new TelegramHandle(telegram),
                    Email.forStudent(email), classes);
            Set<String> keys = new HashSet<>();
            for (JsonAdaptedAttendance entry : attendance) {
                if (entry == null) {
                    throw new IllegalValueException("Attendance entries must not be null.");
                }
                AttendanceRecord record = entry.toModelType();
                if (!keys.add(record.course + ":" + record.week)) {
                    throw new IllegalValueException("Duplicate attendance for " + record.course
                            + " week " + record.week + ".");
                }
                student = student.withAttendance(record);
            }
            return student;
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
