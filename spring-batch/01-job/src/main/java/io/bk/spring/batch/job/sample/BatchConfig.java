package io.bk.spring.batch.job.sample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job sampleJob() {
        return new JobBuilder("sampleJob", jobRepository)
            .start(sampleStep())
            .build();
    }


    @Bean
    public Step sampleStep() {
        return new StepBuilder("sampleStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                // JobExecutionContext에서 이전 실행 상태 확인
                ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();

                if (!stepContext.containsKey("RETRY_SUCCESS")) {
                    String runDate = (String) chunkContext.getStepContext().getJobParameters().get("runDate");
                    if ("2024-12-16".equals(runDate)) {
                        System.out.println("Simulating failure for runDate=2024-12-16");
                        stepContext.put("RETRY_SUCCESS", false); // 실패 상태 저장
                        throw new RuntimeException("Simulated failure for runDate=2024-12-16");
                    }
                }

                System.out.println("Step executed successfully for runDate: " +
                    chunkContext.getStepContext().getJobParameters().get("runDate"));
                stepContext.put("RETRY_SUCCESS", true); // 성공 상태 저장
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
