package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalData {
    // Shared list of task/schedule names (for the menu buttons)
    public static List<String> taskNames = new ArrayList<>();

    // Shared list of the full schedule objects
    public static List<TaskScheduler> schedules = new ArrayList<>();
    
    public static List<TaskScheduler> prioTasks = new ArrayList<>(); 
    
    public static List<TaskScheduler> getPriorityTasks() {
        return schedules.stream()
                .filter(TaskScheduler::isPriority)  // Only tasks marked as priority
                .collect(Collectors.toList());
    }
}