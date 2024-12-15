package io.bk.spring.batch.job.sample;

import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
public class JobRunner {

    private final JobLauncher jobLauncher;
    private final Job sampleJob;

    public JobRunner(JobLauncher jobLauncher, Job sampleJob) {
        this.jobLauncher = jobLauncher;
        this.sampleJob = sampleJob;
    }

    public void runJob(String parameterValue) throws Exception {
        // Spring Batch 5.x에 맞는 JobParameter 생성
        JobParameters jobParameters = new JobParameters(
            Map.of(
                "runDate", new JobParameter<>(parameterValue, String.class, true) // identifying true
            )
        );

        JobExecution jobExecution = jobLauncher.run(sampleJob, jobParameters);
        System.out.println("Job Execution ID: " + jobExecution.getId());
        System.out.println("Job Instance ID: " + jobExecution.getJobInstance().getId());
        System.out.println("Job Instance Name: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Job Execution Status: " + jobExecution.getStatus());
    }
}
