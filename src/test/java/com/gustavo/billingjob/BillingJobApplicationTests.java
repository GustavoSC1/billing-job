package com.gustavo.billingjob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
// Usado para capturar as saídas padrão(stdout) e as saídas de erro(stderr) no console geradas durante a execução do teste. 
@ExtendWith(OutputCaptureExtension.class)
class BillingJobApplicationTests {

	@Autowired
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Test
	void testJobExecution(CapturedOutput output) throws Exception {
		// given
		// O JobParametersBuilder é a principal API fornecida pelo Spring Batch para construir um conjunto de JobParameters.
		// Usamos esse construtor para criar um parâmetro do tipo String chamado input.file com o valor /some/input/file.
		// Usamos também para criar um parâmetro do tipo String chamado file.format com o valor csv, além disso informamos 
		// que o parâmetro file.format não é identificador.
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("input.file", "/some/input/file")
				.addString("file.format", "csv", false)
				.toJobParameters();

		// when
		// Inicia o Job em lote com um conjunto de parâmetros.
		JobExecution jobExecution = this.jobLauncher.run(this.job, jobParameters);

		// then
		Assertions.assertTrue(output.getOut().contains("processing billing information from file /some/input/file"));

		Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());	
	}

}
