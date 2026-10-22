package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.AttendanceSummary;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private ClassMembership attendanceClass;
    private AttendanceWeek attendanceWeek;

    @FXML
    private Label rosterHeading;
    @FXML
    private Label rosterSummary;
    private final Label emptyRoster = new Label();

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label studentDetails;
    @FXML
    private ScrollPane studentDetailsPane;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        emptyRoster.setWrapText(true);
        emptyRoster.setStyle("-fx-text-fill: white; -fx-padding: 12;");
        personListView.setPlaceholder(emptyRoster);
        personList.addListener((ListChangeListener<Person>) change -> updateRosterHeading());
        updateRosterHeading();
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            boolean studentSelected = selected != null && selected.isStudent();
            studentDetailsPane.setManaged(studentSelected);
            studentDetailsPane.setVisible(studentSelected);
            studentDetails.setText(studentSelected ? selected.getName() + "\n"
                    + selected.getTelegramHandle().orElseThrow() + " | " + selected.getEmail() + "\n"
                    + selected.getMemberships() + "\n" + AttendanceFormatter.details(selected) : "");
        });
    }

    /**
     * Roster integration hook: call after a successful class/week query; pass nulls on list/name search.
     * Refreshes all visible badges, even when changing only the week leaves the student list unchanged.
     */
    public void setAttendanceContext(ClassMembership membership, AttendanceWeek week) {
        if ((membership == null) != (week == null)) {
            throw new IllegalArgumentException("Attendance class and week must be supplied together.");
        }
        attendanceClass = membership;
        attendanceWeek = week;
        personListView.refresh();
        updateRosterHeading();
    }

    private void updateRosterHeading() {
        int count = personListView.getItems().size();
        String number = count + (count == 1 ? " student" : " students");
        rosterHeading.setText(attendanceClass == null ? "All students | " + number
                : attendanceClass + " | Week " + attendanceWeek + " | " + number);
        emptyRoster.setText(attendanceClass == null ? "No students saved. Use add to create a student."
                : "No students found in " + attendanceClass + ". Use list to view all students.");
        updateRosterSummary(count);
    }

    /** Shows the class's attendance totals for the viewed week, only in a non-empty class view. */
    private void updateRosterSummary(int count) {
        boolean showSummary = attendanceClass != null && count > 0;
        rosterSummary.setVisible(showSummary);
        rosterSummary.setManaged(showSummary);
        rosterSummary.setText(showSummary
                ? AttendanceSummary.of(personListView.getItems(), attendanceClass, attendanceWeek).toString() : "");
    }

    /** Clears both the selected row and its details after a successful query or deletion. */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
        studentDetails.setText("");
        studentDetailsPane.setVisible(false);
        studentDetailsPane.setManaged(false);
    }

    /** Selects and reveals a student after a successful persisted addition. */
    public void selectPerson(Person person) {
        personListView.getSelectionModel().select(person);
        personListView.scrollTo(person);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, attendanceClass, attendanceWeek).getRoot());
            }
        }
    }

}
