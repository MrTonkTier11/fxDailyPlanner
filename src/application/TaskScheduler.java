package application;

import java.util.List;

public class TaskScheduler {
    private String note;
    private String date; // Month/Day
    private String time; // Hour/Mins
    private List<String> daysOfWeek; // e.g., ["M", "W", "F"]

    public TaskScheduler(String note, String date, String time, List<String> daysOfWeek) {
        this.note = note;
        this.date = date;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
    }

    // Getters and Setters (omitted for brevity, but needed in a real app)
    public String getNote() { return note; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public List<String> getDaysOfWeek() { return daysOfWeek; }
}