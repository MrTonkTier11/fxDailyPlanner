package application;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable; // Often required for persistence (saving/loading)

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
	    private boolean priority;
	    
	    //  NEW FIELD FOR PAUSE/RESUME FUNCTIONALITY 
        private boolean isPaused = false;
        // private long remainingDurationSeconds = -1; // Added if you needed complex pause/resume logic

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
	    public boolean isPriority() { return priority; }
	    
	    
	    // Setters
	    public void setNote(String note) { this.note = note; }
	    public void setNoteDetail(String noteDetail) { this.noteDetail = noteDetail; }
	    public void setStartHour(int startHour) { this.startHour = startHour; }
	    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
	    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
	    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
	    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
	    public void setRecurringDays(List<String> recurringDays) { this.recurringDays = recurringDays; }

	    
	    
	    //  NEW: Paused state getter
        public boolean isPaused() { return isPaused; }	    

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
	    public void setPriority(boolean priority) {
	        this.priority = priority;
	    }
	    
	    //  NEW: Paused state setter
        public void setPaused(boolean isPaused) {
            this.isPaused = isPaused;
            // Optionally, reset currentStartTime when pausing to stop the timer
            if (isPaused) {
                // To keep track of the remaining time for accurate resume,
                // you would calculate remaining time here and store it.
                // For a simple pause/resume, just stopping the clock visually is often enough.
                // We'll leave currentStartTime intact for now to simplify, 
                // and let the controller handle visual status.
            }
        }
	}