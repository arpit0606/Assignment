package com.cs.demo.config;

import org.springframework.batch.item.file.LineMapper;

import com.cs.demo.model.LogEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogEventJSONLineMapper implements LineMapper<LogEvent> {

    private ObjectMapper mapper = new ObjectMapper();


    /**
     * Interpret the line as a Json object and create a Blub Entity from it.
     * 
     * @see LineMapper#mapLine(String, int)
     */
    @Override
    public LogEvent mapLine(String line, int lineNumber) throws Exception {
        return mapper.readValue(line, LogEvent.class);
    }

}