package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path directory;

    @Test
    public void readJsonFile_trailingContent_rejectsWithoutChangingFile() throws Exception {
        Path file = directory.resolve("trailing.json");
        // A second valid JSON root must be rejected just like malformed trailing text.
        for (String suffix : new String[]{"\nTHIS IS NOT VALID JSON", " {}", " null", " []", " 42"}) {
            String json = SerializableTestClass.JSON_STRING_REPRESENTATION + suffix;
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void readJsonFile_trailingWhitespace_loadsObject() throws Exception {
        Path file = directory.resolve("whitespace.json");
        Files.writeString(file, SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n");
        SerializableTestClass actual = JsonUtil.readJsonFile(file, SerializableTestClass.class).orElseThrow();
        assertEquals(SerializableTestClass.getNameTestValue(), actual.getName());
        assertEquals(SerializableTestClass.getListTestValues(), actual.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), actual.getMapOfIntegerToString());
    }

    @Test
    public void readJsonFile_nullRoot_rejectsWithoutChangingFile() throws Exception {
        Path file = directory.resolve("null.json");
        Files.writeString(file, "null");
        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
        assertEquals("null", Files.readString(file));
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
