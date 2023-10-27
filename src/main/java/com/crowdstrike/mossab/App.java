package com.crowdstrike.mossab;

import com.crowdstrike.mossab.processor.CsvProcessor;

import java.util.Arrays;
import java.util.List;

/***
 * The main entry point to the code. URLs are passed in as command line arguments and are passed into the CsvProcessor
 * to read the files concurrently (via ThreadPoolExecutor) and compute the median/average.
 */
public class App {
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Please add at least 1 URL/file via the command line, e.g. java -jar target/CrowdStrike-Homework-Mossab-1.0-SNAPSHOT.jar [list of urls or files, space separated]");
            return;
        }

        // URLs are captured as command line arguments, space separated - captured in a list and sent to processor
        List<String> urls = Arrays.asList(args);

        CsvProcessor processor = new CsvProcessor(urls);

        // where the all the heavy lifting occurs. CSVs are read and median/average is computed. Time taken is also tracked
        processor.process();

        processor.printMedianAndAverageAges();

        processor.printMetrics();

        processor.printFileSummaries();
    }
}
