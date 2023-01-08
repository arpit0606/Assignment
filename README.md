# How to run
This is a spring boot service build using maven. It uses spring-batch to read the log file and process in batch mode for non-interactive bulk processing
Uses scheduler cron expression to trigger the batch job. 

if using IDE to run 
1. simply git clone
2. import the maven project in IDE
3. go to file App.java, right click and run . It will start the springboot application and the bacth job will be trigger every 1 min. (enhancement to be maid to trigger the batch job only in case of there is change in the directory/logfile using java nio.file.watchservice.
To see the results of the batch job 
4. go to http://localhost:8080/console/ and loginto h2 console using url='jdbc:h2:mem:testdb username='sa' password=<empty>
