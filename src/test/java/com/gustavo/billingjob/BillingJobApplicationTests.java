package com.gustavo.billingjob;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

// Ativa a configuração de beans específicos para testes do Spring Batch, permitindo que componentes como o 
// JobLauncherTestUtils e JobRepositoryTestUtils estejam disponíveis automaticamente no contexto de teste.
@SpringBatchTest
@SpringBootTest
class BillingJobApplicationTests {
	
	// Facilita a execução de jobs e steps dentro dos testes. Um dos recursos é que ele detecta automaticamente o job em teste no 
	// contexto do aplicativo se ele for único. Este é o caso em nosso Lab, temos apenas um único job definido que é o BillingJob. 
	// Por esse motivo, podemos remover a autowiring do job em teste da classe de teste.
	@Autowired
   	private JobLauncherTestUtils jobLauncherTestUtils;

   	@Autowired
   	// Fornece métodos para manipular o JobRepository durante os testes
   	private JobRepositoryTestUtils jobRepositoryTestUtils;
   	
   	@Autowired
    private JdbcTemplate jdbcTemplate;
   	
   	// Caso haja mais de um job, é necessário especificar qual deles será testado. 
   	/*
    @Autowired
    @Qualifier("mySpecificJob")
    public void setJob(Job job) {
        this.jobLauncherTestUtils.setJob(job);
    }*/
   	
   	@BeforeEach
	public void setUp() {		
   		// Limpa todas as execuções de trabalho (job executions) antes de cada teste, para que cada execução tenha um esquema novo 
   		// e não seja afetada pelos metadados de outros testes.
		this.jobRepositoryTestUtils.removeJobExecutions();
		// Exclui todas as linhas da tabela BILLING_DATA antes de cada teste
		JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "BILLING_DATA");
	}

	@Test
	// Neste teste, passamos o arquivo de entrada como um parâmetro de trabalho e esperamos que o arquivo esteja presente 
	// no diretório de preparação após a execução do trabalho.
	void testJobExecution() throws Exception {
		// given
		// Cria um JobParameters que inclui um parâmetro 'random' exclusivo, geralmente com base no timestamp, para garantir que 
		// cada execução seja considerada única. Além disso, diciona um parâmetro personalizado chamado "input.file"
		JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParametersBuilder()
		        .addString("input.file", "input/billing-2023-01.csv")
		        .addString("output.file", "staging/billing-report-2023-01.csv")
		        .addString("data.year", "2023")
		        .addString("data.month", "1")
		        .toJobParameters();

		// when
		// Inicia o Job em lote com um conjunto de parâmetros.
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

		// then
		Path billingReport = Paths.get("staging", "billing-report-2023-01.csv");
		   
		Assertions.assertTrue(Files.exists(Paths.get("staging", "billing-2023-01.csv")));
		Assertions.assertEquals(1000, JdbcTestUtils.countRowsInTable(jdbcTemplate, "BILLING_DATA"));		
		Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());	
		Assertions.assertTrue(Files.exists(billingReport));
		Assertions.assertEquals(781, Files.lines(billingReport).count());
	}

}
