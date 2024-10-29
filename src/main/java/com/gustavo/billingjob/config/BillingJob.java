package com.gustavo.billingjob.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobRepository;

public class BillingJob implements Job {

    private JobRepository jobRepository;

    public BillingJob(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public String getName(){
        return "BillingJob";
    }

    @Override
    public void execute(JobExecution execution) {
    	try {
    		JobParameters jobParameters = execution.getJobParameters();
    		// Extraindo o parâmetro input.file do JobParameters
	        String inputFile = jobParameters.getString("input.file");
	        System.out.println("processing billing information from file " + inputFile); 
	        // É responsabilidade da implementação do Job relatar seu status ao JobRepository.
	        execution.setStatus(BatchStatus.COMPLETED);	
	        execution.setExitStatus(ExitStatus.COMPLETED);
    	} catch (Exception exception) {
            execution.addFailureException(exception);

            execution.setStatus(BatchStatus.COMPLETED);

            execution.setExitStatus(ExitStatus.FAILED.addExitDescription(exception.getMessage()));
        } finally {
        	this.jobRepository.update(execution);
        }
    }
    
}

