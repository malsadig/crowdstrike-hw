package com.crowdstrike.mossab.processor;

import com.crowdstrike.mossab.model.CsvFile;
import com.crowdstrike.mossab.model.CsvFileStatus;
import com.crowdstrike.mossab.model.Person;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CvsProcessorTest {

    @Test
    public void testHttpsSimpons_shouldWork() {
        List<String> urls = new ArrayList<>();
        urls.add("https://pastebin.com/raw/JN0qTCpZ");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        // check that the processor crunched numbers correctly
        assertEquals(processor.getFiles().size(), 1);
        assertEquals(processor.getExecutorService().getLargestPoolSize(), 1); // 1 thread should have been used
        assertEquals(processor.getMedianAge(), 39.00, 0.01);
        assertEquals(processor.getAverageAge(), 28.66, 0.01);
        assertNotNull(processor.getMedianPerson());

        // median person can be Marge or Homer, as both have the median age of 39, but cannot be Lisa
        assertNotEquals(processor.getMedianPerson().getFirstName(), "Lisa");

        CsvFile file = processor.getFiles().get(0);

        checkSimpsonsFile(file, urls);
    }

    @Test
    public void testHttpsSimponsAndSevenLocalCsvs_shouldWorkWithMalformedData() {
        System.out.println(new File("./src/data/file1.csv").getAbsolutePath());
        List<String> urls = new ArrayList<>();
        urls.add("https://pastebin.com/raw/JN0qTCpZ");
        urls.add("./src/data/file1.csv");
        urls.add("./src/data/file2.csv");
        urls.add("./src/data/file3.csv");
        urls.add("./src/data/file4.csv");
        urls.add("./src/data/file5.csv");
        urls.add("./src/data/file6_bad.csv");
        urls.add("./src/data/file9_bad.csv");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        // check that the processor crunched numbers correctly
        assertEquals(processor.getFiles().size(), 8);
        assertEquals(processor.getExecutorService().getLargestPoolSize(), 8); // 1 thread should have been used
        assertEquals(processor.getMedianAge(), 31.00, 0.01);
        assertEquals(processor.getAverageAge(), 33.8, 0.01);
        assertNotNull(processor.getMedianPerson());

        CsvFile simpsonsFile = processor.getFiles().get(0);

        checkSimpsonsFile(simpsonsFile, urls);

        CsvFile file1 = processor.getFiles().get(1);

        assertEquals(file1.getPeople().size(), 1000);
        assertEquals(file1.getMalformedData().size(), 0);
        assertNull(file1.getResponseCode());
        assertEquals(file1.getUrl(), urls.get(1));
        assertEquals(file1.getStatus(), CsvFileStatus.PROCESSED_VALID);

        CsvFile file2 = processor.getFiles().get(2);

        assertEquals(file2.getPeople().size(), 1000);
        assertEquals(file2.getMalformedData().size(), 0);
        assertNull(file2.getResponseCode());
        assertEquals(file2.getUrl(), urls.get(2));
        assertEquals(file2.getStatus(), CsvFileStatus.PROCESSED_VALID);

        CsvFile file3 = processor.getFiles().get(3);

        assertEquals(file3.getPeople().size(), 10000);
        assertEquals(file3.getMalformedData().size(), 0);
        assertNull(file3.getResponseCode());
        assertEquals(file3.getUrl(), urls.get(3));
        assertEquals(file3.getStatus(), CsvFileStatus.PROCESSED_VALID);

        CsvFile file4 = processor.getFiles().get(4);

        assertEquals(file4.getPeople().size(), 1000);
        assertEquals(file4.getMalformedData().size(), 0);
        assertNull(file4.getResponseCode());
        assertEquals(file4.getUrl(), urls.get(4));
        assertEquals(file4.getStatus(), CsvFileStatus.PROCESSED_VALID);

        CsvFile file5 = processor.getFiles().get(5);

        assertEquals(file5.getPeople().size(), 1000);
        assertEquals(file5.getMalformedData().size(), 0);
        assertNull(file5.getResponseCode());
        assertEquals(file5.getUrl(), urls.get(5));
        assertEquals(file5.getStatus(), CsvFileStatus.PROCESSED_VALID);

        CsvFile file6 = processor.getFiles().get(6);

        assertEquals(file6.getPeople().size(), 0);
        assertEquals(file6.getMalformedData().size(), 0);
        assertNull(file6.getResponseCode());
        assertEquals(file6.getUrl(), urls.get(6));
        assertEquals(file6.getStatus(), CsvFileStatus.EMPTY);

        CsvFile file7 = processor.getFiles().get(7);

        assertEquals(file7.getPeople().size(), 45);
        assertEquals(file7.getMalformedData().size(), 3);
        assertNull(file7.getResponseCode());
        assertEquals(file7.getUrl(), urls.get(7));
        assertEquals(file7.getStatus(), CsvFileStatus.PROCESSED_WITH_INVALID_INPUT);
    }

    @Test
    public void testNullUrl_shouldNotWork() {
        List<String> urls = new ArrayList<>();
        urls.add(null);

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        checkEmptyProcessor(processor, urls);

        assertNull(processor.getFiles().get(0).getResponseCode());

        assertEquals(processor.getFiles().get(0).getStatus(), CsvFileStatus.NULL_OR_EMPTY_URL);
    }

    @Test
    public void testMalformedOrInvalidFile_shouldNotWork() {
        List<String> urls = new ArrayList<>();
        urls.add("some malformed url");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        checkEmptyProcessor(processor, urls);

        assertNull(processor.getFiles().get(0).getResponseCode());

        assertEquals(processor.getFiles().get(0).getStatus(), CsvFileStatus.FILE_NOT_FOUND);
    }

    @Test
    public void testMalformedUrl_shouldNotWork() {
        List<String> urls = new ArrayList<>();
        urls.add("https://some malformed url");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        checkEmptyProcessor(processor, urls);

        assertNull(processor.getFiles().get(0).getResponseCode());

        // check that the processor crunched numbers correctly
        assertEquals(processor.getFiles().get(0).getStatus(), CsvFileStatus.IO_EXCEPTION_RETRIEVING_FILE);
    }

    @Test
    public void testFileNotFoundLocallyUrl_shouldNotWork() {
        List<String> urls = new ArrayList<>();
        urls.add("file:///Users/nobody/nonexistentFile.csv");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        checkEmptyProcessor(processor, urls);

        assertNull(processor.getFiles().get(0).getResponseCode());

        // check that the processor crunched numbers correctly
        assertEquals(processor.getFiles().get(0).getStatus(), CsvFileStatus.FILE_NOT_FOUND);
    }

    @Test
    public void testBadReturnCode_shouldNotWork() {
        List<String> urls = new ArrayList<>();
        urls.add("https://pastebin.com/raw/JN0qTCpZWLEKJAPOJFEAGE");

        CsvProcessor processor = new CsvProcessor(urls);
        processor.process();

        checkEmptyProcessor(processor, urls);

        assertEquals(processor.getFiles().get(0).getResponseCode().intValue(), 404);

        // check that the processor crunched numbers correctly
        assertEquals(processor.getFiles().get(0).getStatus(), CsvFileStatus.BAD_RETURN_CODE);
    }

    public void checkEmptyProcessor(CsvProcessor processor, List<String> urls) {
        assertEquals(processor.getFiles().size(), 1);
        assertEquals(processor.getExecutorService().getLargestPoolSize(), 1); // 1 thread should have been used
        assertEquals(processor.getMedianAge(), 0.00, 0.01);
        assertEquals(processor.getAverageAge(), 0.00, 0.01);
        assertNull(processor.getMedianPerson());

        CsvFile file = processor.getFiles().get(0);

        // check that CsvFile was constructed properly
        assertEquals(file.getPeople().size(), 0);
        assertEquals(file.getMalformedData().size(), 0);
        assertEquals(file.getUrl(), urls.get(0));
    }

    public void checkSimpsonsFile(CsvFile file, List<String> urls) {
        // check that CsvFile was constructed properly
        assertEquals(file.getPeople().size(), 3);
        assertEquals(file.getMalformedData().size(), 0);
        assertEquals(file.getResponseCode().intValue(), 200);
        assertEquals(file.getUrl(), urls.get(0));
        assertEquals(file.getStatus(), CsvFileStatus.PROCESSED_VALID);

        List<Person> people = file.getPeople();

        Person homer = people.get(0);

        // check that people/lines were correctly read
        assertEquals(homer.getFirstName(), "Homer");
        assertEquals(homer.getLastName(), "Simpson");
        assertEquals(homer.getAge(), 39);

        Person marge = people.get(1);

        assertEquals(marge.getFirstName(), "Marge");
        assertEquals(marge.getLastName(), "Simpson");
        assertEquals(marge.getAge(), 39);

        Person lisa = people.get(2);

        assertEquals(lisa.getFirstName(), "Lisa");
        assertEquals(lisa.getLastName(), "Simpson");
        assertEquals(lisa.getAge(), 8);
    }
}
