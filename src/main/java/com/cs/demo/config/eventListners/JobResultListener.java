package com.cs.demo.config.eventListners;

import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.cs.demo.config.BatchConfig;

import ch.qos.logback.classic.Logger;

@Component
public class JobResultListener implements JobExecutionListener {

	private final Logger logger = (Logger) LoggerFactory.getLogger(JobResultListener.class);
	@Value("${input.dir}")
    private Resource inputResource;
	
	public void beforeJob(JobExecution jobExecution) {
		logger.info("------ Called beforeJob()------");
	}

	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED ) {
	        //job success
			logger.info("job with job id: "+jobExecution.getJobId()+ " SUCCESS");
			
			//Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    }
	    else if (jobExecution.getStatus() == BatchStatus.FAILED) {
	        //job failure
	    	logger.info("job with job id: "+jobExecution.getJobId()+ " faileds");
	    	
	    }
	}
}