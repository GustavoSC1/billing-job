package com.gustavo.billingjob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
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
		JobParameters jobParameters = new JobParameters();

		// when
		// Inicia o Job em lote com um conjunto de parâmetros.
		JobExecution jobExecution = this.jobLauncher.run(this.job, jobParameters);

		// then
		Assertions.assertTrue(output.getOut().contains("processing billing information"));

		Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());	
	}

}
