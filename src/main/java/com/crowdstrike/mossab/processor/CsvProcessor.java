package com.crowdstrike.mossab.processor;

import com.crowdstrike.mossab.model.CsvFile;
import com.crowdstrike.mossab.model.Person;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class CsvProcessor {
    private final List<String> urls;
    private final List<Person> people;
    private final List<CsvFile> files;
    private ThreadPoolExecutor executorService;
    private long endTime;
    private long startTime;
    private double medianAge;
    private double averageAge;
    private Person medianPerson;

    public CsvProcessor(List<String> urls) {
        this.urls = urls;
        this.people = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public void process() {
        // a thread pool to be used for concurrency
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

        // used to store all the results of the executed tasks/threads
        List<Future<CsvFile>> csvFileFutures = new ArrayList<>();

        // used to calcuate the elapsed time.
        // nano time is used because discrepancies can occur when using `currentTimeMillis` - best avoided
        this.startTime = System.nanoTime();

        // for each URL, a task is submitted to the thread pool executor to execute on a new/available thread
        // this allows each file to be read and parsed in parallel
        for (String url : urls) {
            // CsvReader does the parsing in parallel and returns a CsvFile object, which has a list of people,
            // a list of malformed lines that were not included in number crunching, and the status of the process for
            // that file. This is returned as a Future to be used when the parallelization is completed
            Future<CsvFile> future = executorService.submit(new CsvReader(url));
            csvFileFutures.add(future);
        }

        // for each future, the resultant CsvFile is stored, in case we want to do something with them later on
        // the list of people from each file is aggregated into one big person list to do the
        for (Future<CsvFile> csvFileFuture : csvFileFutures) {
            try {
                CsvFile csvFile = csvFileFuture.get();

                this.files.add(csvFile);
                people.addAll(csvFile.getPeople());
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("A thread was interrupted or something otherwise went wrong during concurrent execution.");
                throw new RuntimeException(e);
            }
        }

        // to calculate elapsed time
        // note: the instructions readme states "the clock time spent reading all the data files"
        // so I've put this here rather than at the end of the method to exclude the time taken to sort and calculate
        // the medians. this ensures only the time spent reading the data files is included.
        this.endTime = System.nanoTime();

        // executor service is no longer needed at this point. should be shut down
        // if we wanted to do another round of input or if we needed it again in the future in some way, i would leave
        // it open to new tasks
        executorService.shutdown();

        // necessary in order to obtain the median age+person. O(nlogn) where n is the total number of people in all files
        people.sort(Comparator.comparingInt(Person::getAge));

        // calculates and stores the median and averages for later retrieval/printing
        if (!this.people.isEmpty()) {
            this.calculateMedianAndAverages();
        }
    }

    private void calculateMedianAndAverages() {
        // java streams makes this elegant
        this.averageAge = this.people.stream().mapToInt(Person::getAge).average().orElse(0.0);

        // if the number of people is odd, then the median is the middle index
        // if the number of people is even, then the median is the average of the two middle indexes
        int middleIndex = this.people.size() / 2;
        if (this.people.size() % 2 == 0) {
            this.medianAge = (this.people.get(middleIndex - 1).getAge() + this.people.get(middleIndex).getAge()) / 2.0;
        } else {
            this.medianAge = this.people.get(middleIndex).getAge();
        }

        // Get a person with the median age.
        // Note: this may be null in the case of an even number of people wherein no one in the list has the
        // averaged median age.
        this.medianPerson = people.stream().filter(p -> p.getAge() == medianAge).findFirst().orElse(null);
    }

    public void printMedianAndAverageAges() {
        System.out.println("===================================");
        System.out.println("\t\tMEDIAN AND AVERAGE AGES ");
        System.out.println("===================================");
        System.out.println();

        System.out.printf("- The average age is %.2f%n", averageAge);
        System.out.printf("- The median age: %.2f%n", medianAge);
        if (medianPerson != null) {
            System.out.println("- One person with the median age is " + medianPerson.getFirstName() + " " + medianPerson.getLastName());
        } else {
            System.out.println("- There is no person with the median age as the median age is an average of two ages.");
        }

        System.out.println();
    }

    public void printFileSummaries() {
        System.out.println("===================================");
        System.out.println("\t\tFILE SUMMARIES ");
        System.out.println("===================================");
        System.out.println();

        for (int i = 0; i < this.files.size(); i++) {
            CsvFile file = this.files.get(i);
            System.out.println(" File #" + (i + 1) + ":");
            System.out.println("\tURL: " + file.getUrl());
            System.out.println("\tStatus: " + file.getStatus().getStatusMessage());
            if (file.getResponseCode() != null) {
                System.out.println("\tResponse code: " + file.getResponseCode());
            }
            if (!file.getPeople().isEmpty()) {
                System.out.println("\tNumber of people in file (accepted): " + file.getPeople().size());
            }

            if (!file.getMalformedData().isEmpty()) {
                System.out.println("\tNumber of malformed lines / invalid input (rejected): " + file.getMalformedData().size());
            }

            System.out.println();
        }
    }

    public void printMetrics() {
        long elapsedTimeNano = this.endTime - this.startTime;
        // 1 ms = 1M ns
        long elapsedTimeMillis = elapsedTimeNano / 1000000;

        System.out.println("===================================");
        System.out.println("\t\t\tMETRICS");
        System.out.println("===================================");
        System.out.println();

        System.out.println("- Time taken: " + elapsedTimeNano + " nanoseconds (" + elapsedTimeMillis + " milliseconds)");

        // java does a good job of keeping this information handy within the executor service internals
        System.out.println("- Max # of threads used: " + this.executorService.getLargestPoolSize());
        System.out.println();
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<CsvFile> getFiles() {
        return files;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getAverageAge() {
        return averageAge;
    }

    public Person getMedianPerson() {
        return medianPerson;
    }

    public ThreadPoolExecutor getExecutorService() {
        return executorService;
    }
}
