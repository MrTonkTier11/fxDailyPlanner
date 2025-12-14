package application;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TaskDatabase {
	

    private static final String FILE_PATH = "tasks.csv";

    // Save all schedules to CSV
    public static void saveTasks() {
        try (PrintWriter writer = new PrintWriter(FILE_PATH)) {

            for (TaskScheduler task : GlobalData.schedules) {

                String days = String.join(";", task.getRecurringDays());

                // Add priority as last column (true/false)
                writer.println(
                        task.getNote() + "," +
                        task.getStartHour() + "," +
                        task.getStartMinute() + "," +
                        task.getDurationHours() + "," +
                        task.getDurationMinutes() + "," +
                        task.getDurationSeconds() + "," +
                        task.getNoteDetail() + "," +
                        days + "," +
                        task.isPriority() // New column
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load tasks from CSV
    public static void loadTasks() {

        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length >= 9) { // now 9 columns including priority

                    List<String> recurringDays =
                            Arrays.asList(parts[7].split(";"));

                    TaskScheduler task = new TaskScheduler(
                            parts[0],                         // note/title
                            Integer.parseInt(parts[1]),       // start hour
                            Integer.parseInt(parts[2]),       // start minute
                            Integer.parseInt(parts[3]),       // duration hour
                            Integer.parseInt(parts[4]),       // duration minute
                            Integer.parseInt(parts[5]),       // duration second
                            parts[6],                          // note detail
                            recurringDays
                    );

                    // Parse priority column
                    task.setPriority(Boolean.parseBoolean(parts[8]));

                    GlobalData.schedules.add(task);
                    GlobalData.taskNames.add(task.getNote());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all priority tasks
    public static void loadPriorityTasks() {
        GlobalData.prioTasks.clear();
        for (TaskScheduler task : GlobalData.schedules) {
            if (task.isPriority()) GlobalData.prioTasks.add(task);
        }
    }
}
