package com.gustavo.billingjob.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class BillingJobConfiguration {
	
  // O método JobBuilder.start que cria um fluxo de trabalho sequencial (sequential job flow) 
  // e espera o primeiro step da sequência
  @Bean
  public Job job(JobRepository jobRepository, Step step1) {
    return new JobBuilder("BillingJob", jobRepository)
    		.validator(parametersValidator())
    		.start(step1)
    		.build();
  }
  
  // Este gerenciador de transações é configurado automaticamente pelo Spring Boot e podemos 
  // usá-lo aqui para definir o TaskletStep.
  // Um TaskletStep requer um gerenciador de transações para gerenciar a transação em torno 
  // de cada iteração do Tasklet.
  @Bean
  public Step step1(JobRepository jobRepository, JdbcTransactionManager transactionManager) {
	  return new StepBuilder("filePreparation", jobRepository)
			  .tasklet(new FilePreparationTasklet(), transactionManager)
			  .build();
  }
  
  @Bean
  public JobParametersValidator parametersValidator() {
	  return new ParametersValidator();
  }

}