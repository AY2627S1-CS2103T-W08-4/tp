package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final TelegramHandle telegramHandle;
    private final Set<ClassMembership> memberships;
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, tags);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        telegramHandle = null;
        memberships = Collections.emptySet();
    }

    /**
     * Creates a complete student. Legacy phone/address fields are absent, never fabricated.
     * Membership iteration follows input order and each course can occur only once.
     */
    public Person(Name name, TelegramHandle telegramHandle, Email email, Set<ClassMembership> memberships) {
        requireAllNonNull(name, telegramHandle, email, memberships);
        if (memberships.isEmpty()) {
            throw new IllegalArgumentException("A student must have at least one class membership.");
        }
        Set<String> courses = new HashSet<>();
        for (ClassMembership membership : memberships) {
            requireAllNonNull(membership);
            if (!courses.add(membership.courseCode)) {
                throw new IllegalArgumentException("Course " + membership.courseCode
                        + " may be specified only once per student.");
            }
        }
        this.name = Name.forStudent(name.fullName);
        this.email = Email.forStudent(email.value);
        this.telegramHandle = telegramHandle;
        this.memberships = Collections.unmodifiableSet(new LinkedHashSet<>(memberships));
        phone = null;
        address = null;
    }

    public boolean isStudent() {
        return telegramHandle != null;
    }

    public Optional<TelegramHandle> getTelegramHandle() {
        return Optional.ofNullable(telegramHandle);
    }

    public Set<ClassMembership> getMemberships() {
        return memberships;
    }

    public Name getName() {
        return name;
    }

    /** Returns the legacy phone, or null for a student; check isStudent before using it. */
    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    /** Returns the legacy address, or null for a student; check isStudent before using it. */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Detects a contact collision. Students use normalized email or Telegram, never names.
     * Two legacy contacts retain their original name-based identity rule.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        if (otherPerson == null) {
            return false;
        }
        if (isStudent() || otherPerson.isStudent()) {
            return email.value.equalsIgnoreCase(otherPerson.email.value)
                    || telegramHandle != null && telegramHandle.equals(otherPerson.telegramHandle);
        }
        return otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && Objects.equals(phone, otherPerson.phone)
                && email.equals(otherPerson.email)
                && Objects.equals(address, otherPerson.address)
                && tags.equals(otherPerson.tags)
                && Objects.equals(telegramHandle, otherPerson.telegramHandle)
                && memberships.equals(otherPerson.memberships);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, telegramHandle, memberships);
    }

    @Override
    public String toString() {
        if (isStudent()) {
            return new ToStringBuilder(this).add("name", name).add("telegramHandle", telegramHandle)
                    .add("email", email).add("memberships", memberships).toString();
        }
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .toString();
    }

}
