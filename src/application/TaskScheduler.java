package application;

import java.time.LocalDateTime;
import java.util.List;

public class TaskScheduler {
	    
	    private String note;
	    private int startHour;
	    private int startMinute;
	    private int durationHours;
	    private int durationMinutes;
	    private int durationSeconds;
	    private List<String> recurringDays;
	    private LocalDateTime currentStartTime; 
	    private String noteDetail; 
	    private boolean alarmTriggered = false; 

	    public TaskScheduler(String note, int startHour, int startMinute, 
	                         int durationHours, int durationMinutes, int durationSeconds, 
	                         String noteDetail, 
	                         List<String> recurringDays) {
	        this.note = note;
	        this.startHour = startHour;
	        this.startMinute = startMinute;
	        this.durationHours = durationHours;
	        this.durationMinutes = durationMinutes;
	        this.durationSeconds = durationSeconds;
	        this.recurringDays = recurringDays;
	        this.noteDetail = noteDetail; 
	    }

	    // Getters
	    public String getNote() { return note; }
	    public int getStartHour() { return startHour; }
	    public int getStartMinute() { return startMinute; }
	    public int getDurationHours() { return durationHours; }
	    public int getDurationMinutes() { return durationMinutes; }
	    public int getDurationSeconds() { return durationSeconds; }
	    public List<String> getRecurringDays() { return recurringDays; }
	    public LocalDateTime getCurrentStartTime() { return currentStartTime; }
	    public String getNoteDetail() { return noteDetail; }

	    public long getTotalDurationSeconds() {
	        return durationHours * 3600L  
	             + durationMinutes * 60L  
	             + durationSeconds;
	    }

	    public boolean isAlarmTriggered() {
	        return alarmTriggered;
	    }

	    public void setAlarmTriggered(boolean alarmTriggered) {
	        this.alarmTriggered = alarmTriggered;
	    }
	    
	    public void setCurrentStartTime(LocalDateTime currentStartTime) {
	        this.currentStartTime = currentStartTime;
	        if (currentStartTime != null) {
	            this.alarmTriggered = false;
	        }
	    }
	}