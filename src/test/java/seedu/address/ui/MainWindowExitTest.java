package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowExitTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private ExitStorage storage;
    private MainWindow mainWindow;
    private Stage primaryStage;
    private Window helpWindow;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        initializeToolkit();
    }

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BOB);
        model.updateFilteredPersonList(AMY::equals);
        storage = new ExitStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        runOnFxThread(() -> {
            primaryStage = new Stage();
            mainWindow = new MainWindow(primaryStage, logic, storage.getAddressBookFilePath());
            mainWindow.show();
            mainWindow.fillInnerParts();
            mainWindow.handleHelp();
            helpWindow = Window.getWindows().stream().filter(window -> window != primaryStage)
                    .findFirst().orElseThrow();
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> {
            if (helpWindow != null) {
                helpWindow.hide();
            }
            if (primaryStage != null) {
                primaryStage.hide();
            }
        });
    }

    @Test
    public void command_validExit_savesBeforeClosingBothWindows() throws Exception {
        runOnFxThread(() -> {
            storage.beforeSave = this::assertBothWindowsOpen;
            enterCommand("  exit  ");
            assertBothWindowsClosed();
            assertEquals(2, storage.saveCount);
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void command_extraArguments_preservesWindowsDataAndView() throws Exception {
        String expectedSavedData = Files.readString(storage.getAddressBookFilePath());
        runOnFxThread(() -> {
            enterCommand("exit 3");
            assertBothWindowsOpen();
            assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE), feedback());
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertEquals(1, storage.saveCount);
        });
        assertEquals(expectedSavedData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void command_saveFails_keepsBothWindowsOpen() throws Exception {
        String expectedSavedData = Files.readString(storage.getAddressBookFilePath());
        runOnFxThread(() -> {
            storage.failSave = true;
            enterCommand("exit");
            assertBothWindowsOpen();
            assertEquals(ExitCommand.MESSAGE_SAVE_FAILURE, feedback());
            assertEquals(List.of(AMY), model.getFilteredPersonList());
        });
        assertEquals(expectedSavedData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void menu_saveFailsThenRetry_closesOnlyAfterSuccessfulSave() throws Exception {
        runOnFxThread(() -> {
            GuiSettings expectedSettings = model.getGuiSettings();
            storage.failSave = true;
            exitMenu();
            assertBothWindowsOpen();
            assertEquals(ExitCommand.MESSAGE_SAVE_FAILURE, feedback());
            assertEquals(expectedSettings, model.getGuiSettings());

            storage.failSave = false;
            storage.beforeSave = this::assertBothWindowsOpen;
            exitMenu();
            assertBothWindowsClosed();
            assertEquals(3, storage.saveCount);
        });
    }

    @Test
    public void windowClose_saveFails_consumesCloseAndKeepsBothWindowsOpen() throws Exception {
        runOnFxThread(() -> {
            storage.failSave = true;
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            assertBothWindowsOpen();
            assertEquals(ExitCommand.MESSAGE_SAVE_FAILURE, feedback());
            assertEquals(List.of(AMY), model.getFilteredPersonList());
        });
    }

    @Test
    public void windowClose_saveSucceeds_closesBothWindows() throws Exception {
        runOnFxThread(() -> {
            storage.beforeSave = this::assertBothWindowsOpen;
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            assertBothWindowsClosed();
            assertEquals(2, storage.saveCount);
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void enterCommand(String command) {
        TextField commandField = (TextField) primaryStage.getScene().lookup("#commandTextField");
        commandField.setText(command);
        commandField.fireEvent(new ActionEvent());
    }

    private void exitMenu() {
        MenuBar menuBar = (MenuBar) primaryStage.getScene().lookup("#menuBar");
        menuBar.getMenus().get(0).getItems().get(0).fire();
    }

    private String feedback() {
        return ((TextArea) primaryStage.getScene().lookup("#resultDisplay")).getText();
    }

    private void assertBothWindowsOpen() {
        assertTrue(primaryStage.isShowing());
        assertTrue(helpWindow.isShowing());
    }

    private void assertBothWindowsClosed() {
        assertFalse(primaryStage.isShowing());
        assertFalse(helpWindow.isShowing());
    }

    private static class ExitStorage extends JsonAddressBookStorage {
        private boolean failSave;
        private int saveCount;
        private Runnable beforeSave = () -> {};

        ExitStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            beforeSave.run();
            if (failSave) {
                throw new IOException("Simulated disk failure");
            }
            super.saveAddressBook(addressBook);
        }
    }
}
