package com.gustavo.billingjob.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class ParametersValidator implements JobParametersValidator {

	@Override
	public void validate(JobParameters jobParameters) throws JobParametersInvalidException {
		String inputFile = jobParameters.getString("input.file");
		
		if(inputFile == null || inputFile.isEmpty()) {
			throw new JobParametersInvalidException("The 'input.file' parameter is required.");
		}
	}

}
