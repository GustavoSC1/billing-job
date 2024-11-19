package com.gustavo.billingjob.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class BillingJobConfiguration {
	
  // O método JobBuilder.start que cria um fluxo de trabalho sequencial (sequential job flow) 
  // e espera o primeiro step da sequência
  @Bean
  public Job job(JobRepository jobRepository, Step step1, Step step2, Step step3) {
    return new JobBuilder("BillingJob", jobRepository)
    		.validator(parametersValidator())
    		.start(step1)
    		.next(step2)
    		.next(step3)
    		.build();
  }
  
  // Este gerenciador de transações é configurado automaticamente pelo Spring Boot e podemos 
  // usá-lo aqui para definir o TaskletStep. Ele controla diretamente o commit e o rollback 
  // das transações no nível de conexão JDBC.
  // Um TaskletStep requer um gerenciador de transações para gerenciar a transação em torno 
  // de cada iteração do Tasklet, associadas às operações de leitura, processamento e gravação de dados.
  @Bean
  public Step step1(JobRepository jobRepository, JdbcTransactionManager transactionManager) {
	  return new StepBuilder("filePreparation", jobRepository)
			  .tasklet(new FilePreparationTasklet(), transactionManager)
			  .build();
  }
  
  @Bean
  public Step step2(
     JobRepository jobRepository, JdbcTransactionManager transactionManager,
     ItemReader<BillingData> billingDataFileReader, ItemWriter<BillingData> billingDataTableWriter,
     BillingDataSkipListener skipListener) {
      return new StepBuilder("fileIngestion", jobRepository)
    		  // Informa ao Spring Bath que o leitor retornará itens do tipo BillingData e que o escritor escreverá itens do tipo 
    		  // BillingData também.
    		  // Um chunk de tamanho 100 significa que 100 itens serão lidos, processados e gravados de uma vez (dentro de uma transação)
    		  // O Transaction Manager assegura que todas as operações em um chunk sejam tratadas como uma única unidade atômica.
              .<BillingData, BillingData>chunk(100, transactionManager)
              .reader(billingDataFileReader)
              .writer(billingDataTableWriter)
              // Habilita a tolerância a falhas no Step
              .faultTolerant()
              // Especifica a FlatFileParseException como uma exceção ignorável
              .skip(FlatFileParseException.class)
              .skipLimit(10)
              // Adiciona um listener para monitorar e reagir a eventos durante o processamento, como skips
              .listener(skipListener)
              .build();
  }
  
  @Bean
  public Step step3(JobRepository jobRepository, JdbcTransactionManager transactionManager,
                             ItemReader<BillingData> billingDataTableReader,
                             ItemProcessor<BillingData, ReportingData> billingDataProcessor,
                             ItemWriter<ReportingData> billingDataFileWriter) {
      return new StepBuilder("reportGeneration", jobRepository)
              .<BillingData, ReportingData>chunk(100, transactionManager)
              .reader(billingDataTableReader)
              .processor(billingDataProcessor)
              .writer(billingDataFileWriter)
              .build();
  }
  
  @Bean
  public JobParametersValidator parametersValidator() {
	  return new ParametersValidator();
  }
  
  @Bean
  // Permite que o bean seja criado uma vez por execução de Step e que seja isolado para essa execução
  @StepScope
  // Por meio de SpEL (Spring Expression Language) é possivel ler o parametro input.file em tempo de execução
  public FlatFileItemReader<BillingData> billingDataFileReader(@Value("#{jobParameters['input.file']}") String inputFile) {
	// A principal função do FlatFileItemReader é ler dados linha a linha de um arquivo e convertê-los em objetos do tipo desejado.
      return new FlatFileItemReaderBuilder<BillingData>()
              .name("billingDataFileReader")
              // Define a origem do arquivo que será lido.
              .resource(new FileSystemResource(inputFile))
              // Utilizado para dividir cada linha em campos individuais.
              .delimited()
              .delimiter(",")
              .names("dataYear", "dataMonth", "accountId", "phoneNumber", "dataUsage", "callDuration", "smsCount")
              .targetType(BillingData.class)
              .build();
  }
  
  @Bean
  // Este writer de itens foi projetado para gravar itens em um banco de dados usando a API JDBC.
  public JdbcBatchItemWriter<BillingData> billingDataTableWriter(DataSource dataSource) {
      String sql = "insert into BILLING_DATA values (:dataYear, :dataMonth, :accountId, :phoneNumber, :dataUsage, :callDuration, :smsCount)";
      return new JdbcBatchItemWriterBuilder<BillingData>()    		  
              .dataSource(dataSource)
              .sql(sql)
              .beanMapped()
              .build();
  }
  
  @Bean
  @StepScope
  public JdbcCursorItemReader<BillingData> billingDataTableReader(DataSource dataSource,
		  @Value("#{jobParameters['data.year']}") String year,
		  @Value("#{jobParameters['data.month']}") String month) {
      String sql = String.format("select * from BILLING_DATA where DATA_YEAR = %s and DATA_MONTH = %s", year, month);
      // A classe JdbcCursorItemReaderBuilder permite ler dados de um banco de dados relacional por meio de consultas SQL, 
      // utilizando um cursor JDBC para recuperar os registros linha por linha.
      return new JdbcCursorItemReaderBuilder<BillingData>()
              .name("billingDataTableReader")
              .dataSource(dataSource)
              .sql(sql)
              // A classe DataClassRowMapper mapeia automaticamente os resultados de consultas SQL para objetos de domínio imutáveis, 
              // como records ou classes com construtores parametrizados.
              .rowMapper(new DataClassRowMapper<>(BillingData.class))
              .build();
  }
  
  @Bean
  public BillingDataProcessor billingDataProcessor() {
      return new BillingDataProcessor();
  }
  
  @Bean
  @StepScope
  public FlatFileItemWriter<ReportingData> billingDataFileWriter(@Value("#{jobParameters['output.file']}") String outputFile) {
          return new FlatFileItemWriterBuilder<ReportingData>()
        	   // arquivo de destino
              .resource(new FileSystemResource(outputFile))
              .name("billingDataFileWriter")
              .delimited()
              .delimiter(",")
              .names("billingData.dataYear", "billingData.dataMonth", "billingData.accountId", "billingData.phoneNumber", "billingData.dataUsage", "billingData.callDuration", "billingData.smsCount", "billingTotal")
              .build();
  }
  
  @Bean
  @StepScope
  public BillingDataSkipListener skipListener(@Value("#{jobParameters['skip.file']}") String skippedFile) {
	  return new BillingDataSkipListener(skippedFile);
  }

}