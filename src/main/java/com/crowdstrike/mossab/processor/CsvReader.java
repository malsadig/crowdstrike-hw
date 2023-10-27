package com.crowdstrike.mossab.processor;

import com.crowdstrike.mossab.model.CsvFile;
import com.crowdstrike.mossab.model.CsvFileStatus;
import com.crowdstrike.mossab.model.Person;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;

/***
 * This is the individual task that reads in a file concurently. This class is passed in to the ThreadPoolExecutor
 * in the CsvProcessor, and returns a CsvFile with the status of the result, as well as all the people and malformed
 * data within the file.
 */
public class CsvReader implements Callable<CsvFile> {
    private final String urlString;

    public CsvReader(String urlString) {
        this.urlString = urlString;
    }

    /***
     * The primary method. Called by the ThreadPoolExecutor when a new thread is started.
     * <p>
     * The file input handles both "file://" and "http(s)://" seamlessly - both for testing purposes and just to have flexibility.
     *
     * @return a CsvFile with all the necessary information (list of Person POJOs, list of malformed lines, and status)
     */
    @Override
    public CsvFile call() {
        // initialization of the CsvFile object that will contain and return all necessary data/info
        CsvFile csvFile = new CsvFile(urlString);

        // sanity checking the URL string for null or empty
        // if so, set proper status and return, as no data can be retrieved
        if (urlString == null || urlString.isEmpty()) {
            csvFile.setStatus(CsvFileStatus.NULL_OR_EMPTY_URL);
            return csvFile;
        }

        // reader can either be an InputStreamReader in the case of http(s) or FileReader in the case of file://
        Reader reader;

        if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
            try {
                // open HttpURLConnection and set the reader to be used later
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // since this is http(s), I store the response code since that is relevant info we'd want to know
                int responseCode = connection.getResponseCode();
                csvFile.setResponseCode(responseCode);

                // if we don't get a 200, nothing to be done - set status and return
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    csvFile.setStatus(CsvFileStatus.BAD_RETURN_CODE);
                    return csvFile;
                }

                reader = new InputStreamReader(connection.getInputStream());
            } catch (IOException e) { // making sure to catch exceptions properly and mark statuses accordingly
                // in production code, these sorts of exceptions would be logged throughout (e.g. logger.log(e.getMessage()))
                csvFile.setStatus(CsvFileStatus.IO_EXCEPTION_RETRIEVING_FILE);
                return csvFile;
            }
        } else {
            try {
                File file = new File(urlString);
                reader = new FileReader(file);
            } catch (FileNotFoundException e) {
                csvFile.setStatus(CsvFileStatus.FILE_NOT_FOUND);
                return csvFile;
            }
        }

        // a BufferedReader is created from the reader, regardless of file or http, and contents are consumed
        BufferedReader bufferedReader = new BufferedReader(reader);
        processReader(bufferedReader, csvFile);

        return csvFile;
    }

    /***
     * Generic method to process CSV files regardless of http or file.
     * @param reader takes in a reader streaming in the contents of the csv
     * @param csvFile uses the CsvFile object to store the processed/parsed csv info (people, malformed data, status)
     */
    private void processReader(BufferedReader reader, CsvFile csvFile) {
        try {
            reader.readLine(); // to skip header

            String line;
            while ((line = reader.readLine()) != null) {
                // here I debated whether to use a library like Jackson or Apache Commons CSV to read in the
                // CSVs, but I opted for writing my own code for two reasons
                // 1) to reduce dependencies as much as possible
                // 2) to have more fine-grained flexibility and control over the implementation of the process

                // this assumes that the CSVs are comma-delimited
                // this could be a faulty assumption depending on how new/other CSVs are created, but seems
                // to be a fair assumption for now. this would need to be expanded to accommodate more delimiter types
                // if the csv were to be different
                String[] personInfo = line.split(",");
                // if there are more than 2 commas/delimiters, the line is malformed and ignored when calculating median/avg
                if (personInfo.length != 3) {
                    csvFile.getMalformedData().add(line);
                    continue;
                }

                // if the age is not a number, then the line is malformed
                try {
                    int age = Integer.parseInt(personInfo[2].trim());
                    Person person = new Person(personInfo[0].trim(), personInfo[1].trim(), age);
                    csvFile.getPeople().add(person);
                } catch (NumberFormatException nfe) {
                    csvFile.getMalformedData().add(line);
                }
            }

            // set statuses after processing
            if (!csvFile.getMalformedData().isEmpty()) {
                csvFile.setStatus(CsvFileStatus.PROCESSED_WITH_INVALID_INPUT);
            } else if (!csvFile.getPeople().isEmpty()) {
                csvFile.setStatus(CsvFileStatus.PROCESSED_VALID);
            } else {
                csvFile.setStatus(CsvFileStatus.EMPTY);
            }
        } catch (IOException e) {
            csvFile.setStatus(CsvFileStatus.IO_EXCEPTION_RETRIEVING_FILE);
        }
    }
}