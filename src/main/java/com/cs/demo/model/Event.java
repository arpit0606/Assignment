package com.cs.demo.model;


public class Event{
 
	String id;
    String eventstarttime;
    String eventendtime;
    String eventduration;
    String eventtype;
    String eventhost;
    String alert;
    
    public Event(String id, String eventstarttime, String eventendtime, String eventduration, String eventtype, String eventhost,String alert) {
    	this.id=id;
    	this.eventstarttime=eventstarttime;
    	this.eventendtime=eventendtime;
    	this.eventduration=eventduration;
    	this.eventtype=eventtype;
    	this.eventhost=eventhost;
    	this.alert=alert;
    }
    
    public Event() {
	}
    
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEventstarttime() {
		return eventstarttime;
	}
	public void setEventstarttime(String eventstarttime) {
		this.eventstarttime = eventstarttime;
	}
	public String getEventendtime() {
		return eventendtime;
	}
	public void setEventendtime(String eventendtime) {
		this.eventendtime = eventendtime;
	}
	public String getEventduration() {
		return eventduration;
	}
	public void setEventduration(String eventduration) {
		this.eventduration = eventduration;
	}
	public String getEventtype() {
		return eventtype;
	}
	public void setEventtype(String eventtype) {
		this.eventtype = eventtype;
	}
	public String getEventhost() {
		return eventhost;
	}
	public void setEventhost(String eventhost) {
		this.eventhost = eventhost;
	}
	public String getAlert() {
		return alert;
	}
	public void setAlert(String alert) {
		this.alert = alert;
	}
	
    
	
}