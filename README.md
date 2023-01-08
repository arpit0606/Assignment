# How to run
This is a spring boot service build using maven. It uses spring-batch to read the log file and process in batch mode for non-interactive bulk processing
Uses scheduler cron expression to trigger the batch job. 

using IDE to run 
1. simply git clone
2. import the Maven project in IDE
3. go to file App.java, right click and run . It will start the springboot application and the bacth job will be trigger every 1 min and process the file named loffile.txt in classpath folder classpath:input/logfile.txt configurable from application.properties file
(enhancement can be maid to trigger the batch job only in case of there is change in the directory/logfile using java nio.file.watchservice.
To see the results of the batch job while springboot application is still running:
4. go to http://localhost:8080/console/ and login to h2 console using url='jdbc:h2:mem:testdb username='sa' password=<empty>
   go to EVENT table and run query SELECT * FROM EVENT to see the full table. 
