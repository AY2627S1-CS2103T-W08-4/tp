package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public class DataLoadingErrorTest {
    @BeforeAll
    public static void initialize() throws Exception {
        initializeToolkit();
    }

    @Test
    public void startupError_displaysDataPathAndRecoveryInstructions() throws Exception {
        Path data = Path.of("sample data", "students.json").toAbsolutePath();
        runOnFxThread(() -> {
            AtomicReference<DialogPane> displayed = new AtomicReference<>();
            Platform.runLater(() -> {
                for (Window window : Window.getWindows().toArray(Window[]::new)) {
                    DialogPane pane = (DialogPane) window.getScene().lookup("#"
                            + UiManager.ALERT_DIALOG_PANE_FIELD_ID);
                    if (pane != null) {
                        displayed.set(pane);
                        window.hide();
                    }
                }
            });
            UiManager.showDataLoadingErrorAndWait(data);
            DialogPane pane = displayed.get();
            assertNotNull(pane);
            assertEquals("TutorTrack could not open its data file.", pane.getHeaderText());
            assertTrue(pane.getContentText().contains(data.toString()));
            assertTrue(pane.getContentText().contains("Your student data has not been changed."));
            assertTrue(pane.getContentText().contains("move the existing file to a safe location first"));
        });
    }
}
