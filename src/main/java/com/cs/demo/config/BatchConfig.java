package com.cs.demo.config;
 
import java.util.List;

import javax.sql.DataSource;
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

import com.cs.demo.model.Event;
import com.cs.demo.model.LogEvent;
import com.cs.demo.util.CalculateDuration;
 
@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	@Autowired
	private DataSource dataSource;
     
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
 
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
 
    @Value("classPath:/input/logfile.txt")
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
        return new FlatFileItemReaderBuilder<LogEvent>()
                .name("fileReader")
                .resource(inputResource)
                .lineMapper(new LogEventJSONLineMapper())
                .build();
    }
    

    @Bean
    public ItemProcessor<LogEvent, Event> processor() {
        return new ItemProcessor<LogEvent, Event>(){
            @Override
            public Event process(LogEvent logEvent) throws Exception {
                if(logEvent!=null){
                	
                	System.out.println("logEvent " + logEvent.getId()+": "+logEvent.getState()+": "+ logEvent.getTimestamp());
                	String updatestartedRec = "update EVENT set EVENTSTARTTIME = ?, EVENTDURATION = ?, ALERT = ? where ID = ?";
                	String updatfinishedRec = "update EVENT set EVENTENDTIME = ?, EVENTDURATION = ?, ALERT = ? where ID = ?";
                	
                	List<Event> events=jdbcTemplate.query("SELECT * FROM EVENT WHERE ID ='"+logEvent.getId()+"'", new BeanPropertyRowMapper<Event>(Event.class));
                    if(events.size()==1) {
                    	// validate if its finished/started accordingly fill/update duration and alert.
                    	// use jdbctemplate to update
                    	System.out.print("record already exist calculating duration and updating");
                    	// 1. calculate duration 
                    	
                    	// 2. calculate flag
                    	long duration=0l;
                    	String alert="false";
                    	if(logEvent.getState().equalsIgnoreCase("STARTED")) {
							// update qury to update eventstarttime, eventduration, flag true if duration > 4 
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
                    
                    
                    //}
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