package io.bk.spring.batch.job;

import io.bk.spring.batch.job.sample.BatchConfig;
import io.bk.spring.batch.job.sample.JobRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBatchJobApplication implements CommandLineRunner {

    private final JobRunner jobRunner;

    public SpringBatchJobApplication(JobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchJobApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running with parameter 2024-12-16...");
        jobRunner.runJob("2024-12-16");

        System.out.println("Running with parameter 2024-12-17...");
        jobRunner.runJob("2024-12-17");

        System.out.println("Re-running with parameter 2024-12-16...");
        jobRunner.runJob("2024-12-16");
    }
}
