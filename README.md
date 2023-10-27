# CrowdStrike Homework Assignment Submission - Mossab Alsadig

## Instructions for running the code

### Prerequisites

1. **Java JDK**: Be sure to have Java JDK 17+ installed. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/downloads/).
2. **Maven**: Be sure to have Maven installed. If not already installed, installation instructions can be found [here](https://maven.apache.org/install.html).

### 1. Clone the Repository

First, clone the repository locally:

```bash
git clone https://github.com/malsadig/crowdstrike-hw.git
cd crowdstrike-hw
```

### 2. Build the Project

Build the project using Maven:

```bash
mvn clean install
```

This will compile the code, run the tests, and package it into a runnable JAR file.

### 3. Run the Application

The runnable jar file will then be found in `target/` and you can run it using the following command:

```bash
java -jar target/CrowdStrike-Homework-Mossab-1.0-SNAPSHOT.jar [list of urls or files, space separated]
```

Replace the list of urls with whichever list you'd like to run it with - the program supports both local files and http(s) URLs.

### Example output

If you'd like to run/test it with the provided csv files, you can do so with the following command:

```bash
java -jar target/CrowdStrike-Homework-Mossab-1.0-SNAPSHOT.jar ./src/data/file1.csv ./src/data/file2.csv ./src/data/file3.csv ./src/data/file4.csv ./src/data/file5.csv ./src/data/file6_bad.csv ./src/data/file9_bad.csv
```

The output you'll receive is:

```text
===================================
                MEDIAN AND AVERAGE AGES 
===================================

- The average age is 33.80
- The median age: 31.00
- One person with the median age is Tyler BLACKWELL

===================================
                        METRICS
===================================

- Time taken: 22460125 nanoseconds (22 milliseconds)
- Max # of threads used: 7

===================================
                FILE SUMMARIES 
===================================

 File #1:
        URL: ./src/data/file1.csv
        Status: This file was processed and contained only valid input. Success!
        Number of people in file (accepted): 1000

 File #2:
        URL: ./src/data/file2.csv
        Status: This file was processed and contained only valid input. Success!
        Number of people in file (accepted): 1000

 File #3:
        URL: ./src/data/file3.csv
        Status: This file was processed and contained only valid input. Success!
        Number of people in file (accepted): 10000

 File #4:
        URL: ./src/data/file4.csv
        Status: This file was processed and contained only valid input. Success!
        Number of people in file (accepted): 1000

 File #5:
        URL: ./src/data/file5.csv
        Status: This file was processed and contained only valid input. Success!
        Number of people in file (accepted): 1000

 File #6:
        URL: ./src/data/file6_bad.csv
        Status: This file contained no valid data. Possibly corrupt or empty.

 File #7:
        URL: ./src/data/file9_bad.csv
        Status: This file was processed successfully but contained some invalid input. Invalid input was ignored.
        Number of people in file (accepted): 45
        Number of malformed lines / invalid input (rejected): 3
```

---

## Discussion

### Why the program looks the way it does

#### Overview

The program looks the way it does because it needed to concurrently process records from multiple CSV files, provided via input, and then compute summary stats like median and average.

I chose to code the program in Java, package it with Maven, and read in the URLS via the command line.

#### Design principles I followed

I adhered to Java conventions for the package/directory structure, the testing, and variable naming and access modifier leveling. In addition, I kept the following in mind as I coded, which reflects in how the code looks:

1. **Modularity**: I used POJOs when loading the CSV data to make it easier for the human eye to follow and reason with, and I used modularized classes with discrete tasks and responsibilities  (`CsvReader` to read in a single URL, for instance) - following good OOP principles. 
2. **Simplicity**: The code is straightforward from a logical perspective and easy to follow, particularly with my use of comments and rational naming of methods & variables. This makes the code easily maintainable by others.
3. **Concurrency**: The code reads the URLs in parallel using a `ThreadPoolExecutor` of variable size. This makes the program efficient in reading many URLs.
4. **Testing**: I added JUnit tests in order to increase both maintainability as well as identifying any problems with the code.
5. **Output**: I took care to make the output of the program visually easy to follow for the user. It is not too verbose, but also presents useful information about the URLs.

#### Design decisions

1. I made the code simple to run by taking in input via the command line.
2. I used a `ThreadPoolExecutor` of variable size to allow for parallel process of URLs. I did one thread per URL, as I felt that to be a good logical discrete chunk. In the case of much larger files, perhaps a thread could be a dataset of X size (perhaps 50k records).
3. I maintained the metrics/stats as variables for access (only while the program is running or programmatically). I did not choose to persist the metrics/stats somehow.

I also make a number of assumptions which I've outlined below, in addition to design decisions I considered but opted not to take. 

---

### How the program was tested

I tested the program by writing a series of JUnit tests that tested (integration testing) the various types of input that the code could receive, as well as the different paths the CSV processor could take. Below are a list of the tests I chose to write:

1. `testHttpsSimpons_shouldWork`: I uploaded the simple Simpsons list to pastebin and used the provided URL to test whether my code could handle files over http(s).
2. `testHttpsSimponsAndSevenLocalCsvs_shouldWorkWithMalformedData`: I then included the 7 local files provided by CrowdStrike. This tested files with some malformed data, valid files, empty files (or files with no valid data), with up to 10K entries in a file.
3. `testNullUrl_shouldNotWork`: Tested a null/empty URL, which should not work.
4. `testMalformedOrInvalidFile_shouldNotWork`: Tested a malformed file name that would fail to retrieve.
5. `testMalformedUrl_shouldNotWork`: Tested a malformed URL that should fail to retrieve with an IO exception.
6. `testFileNotFoundLocallyUrl_shouldNotWork`: Tested a non-existent file, which should yield FILE_NOT_FOUND.
7. `testBadReturnCode_shouldNotWork`: Tested a URL that would return a 404.

Additional testing I considered, but opted not to do, included random generation of massive CSVs (hundreds of thousands of entries) in order to stress test in-memory processing as well as concurrency, but this would have required finding datasets and cleaning them from online, or writing code to do the generation, which seemed beyond the scope of the project.

These tests can be found in `/src/test/java/crowdstrike/mossab/processor/CsvProcessorTest.java`.

---

### Design directions considered and rejected & why

1. I considered making the code a Spring Boot application with an embedded web server. This would have allowed the user to run the code and then hit the server's GET (or likely POST) endpoints with the URLs. This would also allow multiple requests within the same run. However, this was rejected for a few reasons, the primary being that this would make the program much more bulky for not that much gain in functionality. The assignment description stated to reduce dependencies, and depending on SpringBoot and its host of associated libraries would not be in line with that.
2. I considered using a third party library to parse the CSVs (like Apache Commons CSV or OpenCSV), but in the end I opted to write my own code for two reasons 1) to reduce dependencies as much as possible, 2) to have more fine-grained flexibility and control over the implementation of the process. This assumes that the CSVs are comma-delimited. this could be a faulty assumption depending on how new/other CSVs are created, but this seems to be a fair assumption for now. This would need to be expanded to accommodate more delimiter types if the csv were to be different.
3. I considered writing a custom sorting and median calculation algorithm, but rejected this idea due to Java's sort functionality working fine for in memory calculation. However, this would not work in the case of a much larger dataset that could not be computed in memory.
4. I considered having separate systems for retrieving the data and for processing, such as Apache Kafka's pubsub model - however this was rejected due to being overkill for the assignment. This level of modularity and separation of tasks would be good for a larger, distributed system though.
5. I considered using a fixed sized thread pool. I rejected this in favor of a cached thread pool to accommodate the variable count of URLs. So long as the max heap is not overflown, the cached thread pool should allow for faster processing of a number of URLs larger than a potential fixed thread pool size.
6. I considered having a lot more error/debug reporting and logging, but I opted to have the minimal output necessary as described in the assignment description. To compromise on this, I created a `CsvFileStatus` enum that I pretty print in order to show any potential problems with each URL. This kept the verbosity to a minimum while still printing valuable information to the user.

---

### 1. What assumptions did you make in your design? Why?

In my code, I made the following assumptions:

1. I assumed that the number of URLs would be small enough that the number of threads spun up dynamically to accommodate each URL/file would not crash the system/JVM.
2. I assumed that the total number of records/people would fit in memory, and that the computation of the metrics (particularly the sorting of the people list to get the median - O(nlogn)) would run without issue.
3. I assumed that the results would not need to be persisted in any way. If they did, I would have set up a database to store results of runs.
4. I assumed that only one set of URLs is used in each run of the program. I shut down the thread pool executor and the program as a whole - but if multiple runs were needed, I would have structured the program as a server in which many requests can be made.
5. I assumed that if the number of people is even, then the average of the two middle indices should be taken in order to calculate the median.
6. I assumed that the CSVs are, and always will be, comma-delimited. CSVs can be delimited in many ways, and accommodation of those other delimiters would be necessary to expand the scope of accepted CSV types.

---

### 2. How would you change your program if it had to process many files where each file was over 10M records?

In the case of processing many files where each file was over 10M records, I would change the program in the following ways:

1. Increase memory/heap size: In Java, this is done with the `-Xmx` and `-Xms` VM options. However, there are now many more records per URL, in memory computation (even with increased heap) will still not be as feasible - which leads me to:
2. Distributed processing/storage: When the URLs are retrieved, I would store and process the data using Hadoop and store it on Hadoop's underlying HDFS (Hadoop Distributed File System) datastore, which is designed to reliably store very large files and compute with the MapReduce model, which seems a great candidate for the type of computation needed for this program.
3. Batch/Stream processing: Other than Hadoop, I might opt to do batch processing or streaming (say in chunks of 50K records, computing partial averages and medians) and combining at the end to get the final result. This sort of chunking and parallelization would reduce the bottlenecks of reading such large files from disk or fetching large files via the network.
4. Optimize algorithm: I would ensure that the garbage collector is optimized, that the algorithm to compute the stats are optimized, and that the `ThreadPoolExecutor` is optimized for the system it's running on. With very large datasets, like 10M+ records per file, it's necessary to ensure that the system and code are robust. This can be tested with JMeter or other load testing tools.

---

### 3. How would you change your program if it had to process data from more than 20K URLs?

If the program needed to process data from more than 20K URLs, then a few more considerations come into play, including:

1. It would no longer be feasible to use 1 thread per URL, as that would eat up system/JVM resources. I would have to adjust this to use a fixed size thread pool according to system specs.
2. Distributed system: I would implement Apache Kafka to ingest the data and pass that to Hadoop to compute the stats with a MapReduce model. 
3. Asynchronous processing: Currently, the program blocks till URLs are read (in parallel) before computing stats, but with 20K URLs it makes more sense to asynchronously compute individual URL metrics as they're read rather than blocking for all URLs to be read first.
4. Database and caching use: Instead of keeping all the data in memory, I would make use of databases and caching to store the URLs to be read (in case of retries) as well as the data from the URLs, so that connections don't have to be opened again needlessly. This also allows for less computation to be needed in memory.
5. Monitoring & logging: I would add metrics, logging, and monitoring to watch which URLs were successful or not, and why.
6. Retries: I would implement some retry logic and ensure that retry logic is sound in case of any failure.

---

### 4. How would you test your code for production use at scale?

In addition to adding more integration testing, one key area I would test for production use at scale is **performance/load testing**.

To do this, I would use something like JMeter or Gatling to send massive amounts of load to the program. This would give me a good idea of the capability of the system and whether it needs to scale further. It would also let me know its breaking point. I'd also be able to see how the system behaves under heavy load, such as how the concurrency holds up or if there are any deadlocks or problems that occur - which I can then fix.

I would also add a much wider array of test data to see how the program parses different types of data (or faulty data) and how it handles malformed data in a variety of ways, in order to simulate production level data. This would include testing malformed CSVs, unavailable URLs, and network interruptions.

I would ensure that all tests, including unit, integration, and load testing, are carried out on each code push to the CI/CD pipeline.

I would add metrics collection (such as for 404s, successes, 500s, etc), monitoring, and alerting (using Elasticsearch, Logstash, Kibana, and Pagerduty) to ensure that any fires are detected and put out immediately.

Lastly, I would test to ensure that there are no single points of failure. I would check to see what happens if an application server goes down, or if a database goes down, or a cache goes down, etc. I would ensure that there is good failover and replication to make sure no data is lost and that the system does not crash or halt.

---

This was fun! Thanks for giving me the opportunity to go through it.

Cheers,

Mossab