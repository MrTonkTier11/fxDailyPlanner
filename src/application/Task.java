package application;

import java.time.LocalDate;
import java.time.LocalTime;



import java.time.LocalDate;
import java.time.LocalTime;

public class Task {
    private String name;
    private LocalDate date;
    private LocalTime time;
    private String description;

    public Task(String name) {
        this.name = name;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
        this.description = "";
    }

    // getters / setters
    public String getName() { return name; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTime(LocalTime time) { this.time = time; }
    public void setDescription(String desc) { this.description = desc; }

     //NEW!!  (search method)
    public boolean matches(String query) {
        return name.toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
