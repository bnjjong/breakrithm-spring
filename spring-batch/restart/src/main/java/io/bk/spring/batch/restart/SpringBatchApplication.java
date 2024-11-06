package io.bk.spring.batch.restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBatchApplication {
  public static void main(String[] args){
    System.exit( // jvm 종료
        SpringApplication.exit( // Spring Application context 종료 및 상태 코드 반환
          SpringApplication.run(SpringBatchApplication.class, args) // 배치 실행
        )
    );
  }

}
