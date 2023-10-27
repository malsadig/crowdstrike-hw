package com.crowdstrike.mossab.model;

/***
 * An enum to list the potential states/statuses of each file, what might've gone wrong (if anything), and allows
 * for pretty printing of the message/status. This allows me to not have static strings throughout the code and
 * statuses can be edited only in one place to be reflected across the code.
 * <p>
 * CsvFile has a CsvFileStatus
 */
public enum CsvFileStatus {
    UNPROCESSED("This file has not yet been processed."),
    // get rid of malformed and not file or http
    NULL_OR_EMPTY_URL("THe URL provided for this file was null or empty."),
    IO_EXCEPTION_RETRIEVING_FILE("An IO exception occurred while trying to retrieve this file."),
    FILE_NOT_FOUND("This file was not found on local disk."),
    BAD_RETURN_CODE("The return code for the URL provided was not 200."),
    PROCESSED_VALID("This file was processed and contained only valid input. Success!"),
    PROCESSED_WITH_INVALID_INPUT("This file was processed successfully but contained some invalid input. Invalid input was ignored."),
    EMPTY("This file contained no valid data. Possibly corrupt or empty.");

    private final String statusMessage;

    CsvFileStatus(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
