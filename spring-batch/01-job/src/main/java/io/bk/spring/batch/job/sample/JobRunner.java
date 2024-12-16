package io.bk.spring.batch.job.sample;

import java.util.HashMap;
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
        Map<String, JobParameter<?>> parameters = new HashMap<>();
        parameters.put("runDate", new JobParameter<>(parameterValue, String.class, true));
        parameters.put("uniqueId", new JobParameter<>(System.currentTimeMillis(), Long.class, false)); // 고유 파라미터 추가

        JobParameters jobParameters = new JobParameters(parameters);

        try {
            JobExecution jobExecution = jobLauncher.run(sampleJob, jobParameters);
            System.out.println("Job Execution Status: " + jobExecution.getStatus());

            System.out.println("Job Execution ID: " + jobExecution.getId());
            System.out.println("Job Instance ID: " + jobExecution.getJobInstance().getId());
            System.out.println("Job Instance Name: " + jobExecution.getJobInstance().getJobName());
            System.out.println("Job Execution Status: " + jobExecution.getStatus());
        } catch (Exception e) {
            System.err.println("Job failed for runDate: " + parameterValue);
            e.printStackTrace();
        }

    }
}
