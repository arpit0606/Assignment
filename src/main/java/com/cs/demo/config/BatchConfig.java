package com.cs.demo.config;
 
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.cs.demo.config.eventListners.JobResultListener;
import com.cs.demo.model.Event;
import com.cs.demo.model.LogEvent;
import com.cs.demo.util.CalculateDuration;

import ch.qos.logback.classic.Logger;
 
@Configuration
@EnableBatchProcessing
public class BatchConfig {
	private final Logger logger = (Logger) LoggerFactory.getLogger(BatchConfig.class);
	
	@Autowired
	private DataSource dataSource;
     
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
 
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("${input.dir}")
    private Resource inputResource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
 
    @Bean
    public Job readTXTFileJob() {
        return jobBuilderFactory
                .get("readTXTFileJob")
                .incrementer(new RunIdIncrementer())
                .listener(new JobResultListener())
                .start(step())
                .build();
    }
 
    @Bean
    public Step step() {
        return stepBuilderFactory
                .get("step")
                .<LogEvent, Event>chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
     
     
    @Bean
    public FlatFileItemReader<LogEvent> reader() {
    	logger.info("------ Starting FlatFileItemReader------");
        return new FlatFileItemReaderBuilder<LogEvent>()
                .name("fileReader")
                .resource(inputResource)
                .lineMapper(new LogEventJSONLineMapper())
                .build();
    }
    

    @Bean
    public ItemProcessor<LogEvent, Event> processor() {
    	logger.info("------ Starting ItemProcessor------");
        return new ItemProcessor<LogEvent, Event>(){
            @Override
            public Event process(LogEvent logEvent) throws Exception {
            	logger.info("Process Item ------"+logEvent.getId());
                if(logEvent!=null){
                	
                	String updatestartedRec = "update EVENT set EVENTSTARTTIME = ?, EVENTDURATION = ?, ALERT = ? where ID = ?";
                	String updatfinishedRec = "update EVENT set EVENTENDTIME = ?, EVENTDURATION = ?, ALERT = ? where ID = ?";
                	
                	// query DB and chk if input logevent is already present
                	
                	List<Event> events=jdbcTemplate.query("SELECT * FROM EVENT WHERE ID ='"+logEvent.getId()+"'", new BeanPropertyRowMapper<Event>(Event.class));
                    if(events.size()==1) {
                    	// if input log event is present, means either finished or started record is already processed and saved in database
                    	// validate if record type is finished/started, calculate duration and set alert flag and update in database
                    	long duration=0l;
                    	String alert="false";
                    	if(logEvent.getState().equalsIgnoreCase("STARTED")) {
							// update query to update eventstarttime, eventduration, flag true if duration > 4 
                    		duration=CalculateDuration.calculate(events.get(0).getEventendtime(), logEvent.getTimestamp());
                    		if(duration>4) {
                    			alert="true";
                    		}
                    		jdbcTemplate.update(updatestartedRec, logEvent.getTimestamp(), Long.toString(duration), alert, logEvent.getId());
                    		
						}else {
							duration=CalculateDuration.calculate(events.get(0).getEventstarttime(), logEvent.getTimestamp());
							if(duration>4) {
                    			alert="true";
                    		}
							jdbcTemplate.update(updatfinishedRec, logEvent.getTimestamp(), Long.toString(duration), alert, logEvent.getId());
						}
                  
                    }else {
                    	// use builder design pattern to create Event object
                    	Event event = null;
						if(logEvent.getState().equalsIgnoreCase("STARTED")) {
							event = new Event(logEvent.getId()
                            		, logEvent.getTimestamp()
                            		, null
                            		, null
                            		, logEvent.getType()
                            		, logEvent.getHost()
                            		, "false");
						}else {
							event = new Event(logEvent.getId()
                            		, null
                            		, logEvent.getTimestamp()
                            		, null
                            		, logEvent.getType()
                            		, logEvent.getHost()
                            		, "false");
						}
                    	
                            return event;
                    }
                    
                }
                return null;
            }
        };
    }
    
 
    @Bean
    public JdbcBatchItemWriter<Event> writer() {
        JdbcBatchItemWriter<Event> itemWriter = new JdbcBatchItemWriter<Event>();
        itemWriter.setDataSource(dataSource);
        itemWriter.setSql("INSERT INTO EVENT (ID, EVENTSTARTTIME, EVENTENDTIME, EVENTDURATION, EVENTTYPE, EVENTHOST, ALERT) VALUES (:id, :eventstarttime, :eventendtime, :eventduration, :eventtype, :eventhost, :alert) ");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Event>());
        return itemWriter;
    }
     
}