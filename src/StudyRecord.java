import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class StudyRecord implements Serializable {
    private LocalDate date;
    private long totalMinutes;
    
    public StudyRecord() {
        this.date = LocalDate.now();
        this.totalMinutes = 0;
    }
    
    public StudyRecord(LocalDate date, long totalMinutes) {
        this.date = date;
        this.totalMinutes = totalMinutes;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public long getTotalMinutes() {
        return totalMinutes;
    }
    
    public void addMinutes(long minutes) {
        this.totalMinutes += minutes;
    }
    
    public String formatDuration() {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
    
    @Override
    public String toString() {
        return date + " -> " + formatDuration();
    }
    
    public String toFileString() {
        return date.toString() + "," + totalMinutes;
    }
    
    public static StudyRecord fromFileString(String line) {
        String[] parts = line.split(",");
        LocalDate date = LocalDate.parse(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        return new StudyRecord(date, minutes);
    }
}
