package com.cs.demo.config;
 
import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import com.cs.demo.model.Event;
 
//public class DBLogProcessor implements ItemProcessor<String, List<Event>>
//{
//    public List<Event> process(String logfileEvent, List<Event> event) throws Exception
//    {
//        System.out.println("Inserting event : " + event);
//        return event;
//    }
//
//
//}