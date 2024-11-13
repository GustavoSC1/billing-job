package com.gustavo.billingjob.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class ParametersValidator implements JobParametersValidator {

	@Override
	public void validate(JobParameters jobParameters) throws JobParametersInvalidException {
		String inputFile = jobParameters.getString("input.file");
		String dataYear = jobParameters.getString("data.year");
		String dataMonth = jobParameters.getString("data.month");
		String outputFile = jobParameters.getString("output.file");
		
		if(inputFile == null || inputFile.isEmpty()) {
			throw new JobParametersInvalidException("The 'input.file' parameter is required.");
		} else if(dataYear == null || dataYear.isEmpty()) {
			throw new JobParametersInvalidException("The 'data.year' parameter is required.");
		} else if(dataMonth == null || dataMonth.isEmpty()) {
			throw new JobParametersInvalidException("The 'data.month' parameter is required.");
		} else if(outputFile == null || outputFile.isEmpty()) {
			throw new JobParametersInvalidException("The 'output.file' parameter is required.");
		} 
		
		 try {
			 Integer.parseInt(dataYear);
	     } catch (NumberFormatException e) {
	    	 throw new JobParametersInvalidException("The 'data.year' parameter is not a valid integer.");
	     }
		 
		 try {
			 Integer.parseInt(dataMonth);
	     } catch (NumberFormatException e) {
	    	 throw new JobParametersInvalidException("The 'data.month' parameter is not a valid integer.");
	     }
	}

}
