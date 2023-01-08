package com.cs.demo.util;

public class CalculateDuration {
	
	public static long calculate(String time1,String time2) {
		
		long l1=Long.parseLong(time1); 
		long l2=Long.parseLong(time2);
		
		return Math.abs(l1-l2);
		
	}

	

}
