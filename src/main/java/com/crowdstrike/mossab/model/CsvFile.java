package com.crowdstrike.mossab.model;

import java.util.ArrayList;
import java.util.List;

public class CsvFile {
    private final String urlString;
    private final List<Person> people;
    private final List<String> malformedData;
    private CsvFileStatus status;
    private Integer responseCode;

    public CsvFile(String urlString) {
        this.people = new ArrayList<>();
        this.malformedData = new ArrayList<>();
        this.status = CsvFileStatus.UNPROCESSED;
        this.urlString = urlString;
    }

    public List<Person> getPeople() {
        return this.people;
    }

    public void setStatus(CsvFileStatus newStatus) {
        this.status = newStatus;
    }

    public CsvFileStatus getStatus() {
        return this.status;
    }

    public String getUrl() {
        return this.urlString;
    }

    public List<String> getMalformedData() {
        return malformedData;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }
}
