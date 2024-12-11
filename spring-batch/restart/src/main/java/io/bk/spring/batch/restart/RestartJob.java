package io.bk.spring.batch.restart;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class RestartJob {

  private static final int CHUNK_SIZE = 5;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean(name = "helloJob")
  public Job helloJob(@Qualifier("jsonToDatabaseStep") Step step) {
    return new JobBuilder("helloJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(step)
        .build();
  }

  @Bean
  public Step jsonToDatabaseStep(JsonItemReader<UserDto> reader, ItemWriter<UserDto> writer) {
    return new StepBuilder("step", jobRepository)
        .<UserDto, UserDto>chunk(CHUNK_SIZE, transactionManager) // 청크 사이즈 설정
        .reader(reader)
        .writer(writer)
//        .listener(
//            new StepExecutionListener() {
//              @Override
//              public void beforeStep(StepExecution stepExecution) {
//                log.info("step before!!");
//              }
//
//              @Override
//              public ExitStatus afterStep(StepExecution stepExecution) {
//                log.info("index : {}", stepExecution.getReadCount());
//                stepExecution
//                    .getExecutionContext()
//                    .putLong("start.index", stepExecution.getReadCount());
//                return ExitStatus.COMPLETED;
//              }
//            })
//        .listener(
//            new ItemWriteListener<>() {
//
//              @Override
//              public void afterWrite(Chunk<? extends UserDto> items) {
//                ItemWriteListener.super.afterWrite(items);
//              }
//            })
        .faultTolerant()
//        .retryLimit(3)
//        .retry(ParseException.class)
        .skipLimit(100)
        .skip(Exception.class)
        .build();
  }

  @Bean
  @StepScope
  public JsonItemReader<UserDto> jsonItemReader(
      @Value("#{stepExecutionContext['start.index']}") Long startIndex) {

      ObjectMapper objectMapper = new ObjectMapper();
      JacksonJsonObjectReader<UserDto> jsonObjectReader = new JacksonJsonObjectReader<>(UserDto.class);
      jsonObjectReader.setMapper(objectMapper);

    return new JsonItemReaderBuilder<UserDto>()
        .name("jsonItemReader")
        .jsonObjectReader(jsonObjectReader)
        .resource(new ClassPathResource("user.json"))
        .saveState(true) //  현재 상태를 저장
        .currentItemCount(startIndex != null ? startIndex.intValue() : 0)
        .build();
  }

  @Bean
  public ItemWriter<UserDto> writer() {
    // JPA나 JDBC를 사용하여 데이터베이스에 쓰는 로직 구현
    // 예: JpaItemWriter 또는 JdbcBatchItemWriter 사용
    return items -> {
      log.info("Writing {} items", items.size());
      items.forEach(item -> log.info("Item: {}", item));
    };
  }
}

@Slf4j
class SkippableItemReader<T> implements ItemReader<T> {

    private final ItemReader<T> delegate;
    private boolean skipCurrentItem = false;

    public SkippableItemReader(ItemReader<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException {
        if (skipCurrentItem) {
            skipCurrentItem = false;
            return delegate.read();
        }

        try {
            return delegate.read();
        } catch (Exception e) {
            // 예외 처리 로직 (예: 로깅 또는 다른 처리)
            skipCurrentItem = true;
            throw new ParseException("Error reading item", e);
        }
    }
}
