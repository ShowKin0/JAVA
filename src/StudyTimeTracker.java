import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StudyTimeTracker {
    private static final String RECORD_FILE = "study_record.txt";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        initializeRecordFile();
        Scanner scanner = new Scanner(System.in);

        System.out.println("===== 学习时间记录系统 =====");
        System.out.println("今日已学习: " + formatDuration(getTodayStudyTime()));
        System.out.println();

        while (true) {
            System.out.println("请输入操作:");
            System.out.println("1 - 开始计时");
            System.out.println("2 - 退出程序");
            System.out.print("> ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    startStudySession(scanner);
                    break;
                case "2":
                    System.out.println("程序已退出，今日学习总时长: " + formatDuration(getTodayStudyTime()));
                    return;
                default:
                    System.out.println("无效输入，请输入 1 或 2\n");
            }
        }
    }

    private static void startStudySession(Scanner scanner) {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("\n>>> 计时开始于 " + startTime.format(TIME_FORMATTER));
        System.out.println("提示: 计时进行中，每分钟会显示当前学习时长");
        System.out.println("      输入 2 结束当前计时\n");

        AtomicInteger command = new AtomicInteger(0);
        AtomicBoolean running = new AtomicBoolean(true);

        // 后台线程监听用户输入
        Thread inputThread = new Thread(() -> {
            while (running.get()) {
                try {
                    String line = scanner.nextLine().trim();
                    if (line.equals("2")) {
                        command.set(2);
                        break;
                    }
                } catch (NoSuchElementException e) {
                    break;
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        // 主循环：每分钟显示学习时长
        while (running.get()) {
            try {
                Thread.sleep(60000); // 每分钟更新一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // 检查是否收到结束命令
            if (command.get() == 2) {
                break;
            }

            // 显示当前学习时长
            LocalDateTime now = LocalDateTime.now();
            long minutes = Duration.between(startTime, now).toMinutes();
            System.out.println("当前学习时长: " + formatDuration(minutes));
        }

        // 结束计时
        running.set(false);
        inputThread.interrupt();

        LocalDateTime endTime = LocalDateTime.now();
        long sessionMinutes = Duration.between(startTime, endTime).toMinutes();

        System.out.println("\n<<< 计时结束于 " + endTime.format(TIME_FORMATTER));
        System.out.println("本次学习时长: " + formatDuration(sessionMinutes));

        // 保存记录
        saveStudyRecord(sessionMinutes);
        System.out.println("今日累计学习: " + formatDuration(getTodayStudyTime()));
        System.out.println();
    }

    private static void initializeRecordFile() {
        File file = new File(RECORD_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("创建记录文件失败: " + e.getMessage());
            }
        }
    }

    private static void saveStudyRecord(long minutes) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        long todayTotal = getTodayStudyTime() + minutes;

        // 读取所有记录
        StringBuilder content = new StringBuilder();
        boolean foundToday = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(RECORD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(today + ",")) {
                    content.append(today).append(",").append(todayTotal).append("\n");
                    foundToday = true;
                } else {
                    content.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            // 文件可能不存在，忽略错误
        }

        // 如果今天没有记录，添加新记录
        if (!foundToday) {
            content.append(today).append(",").append(todayTotal).append("\n");
        }

        // 写回文件
        try (FileWriter writer = new FileWriter(RECORD_FILE)) {
            writer.write(content.toString());
        } catch (IOException e) {
            System.err.println("保存记录失败: " + e.getMessage());
        }
    }

    private static long getTodayStudyTime() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        try (BufferedReader reader = new BufferedReader(new FileReader(RECORD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(today)) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            // 忽略错误，返回0
        }

        return 0;
    }

    private static String formatDuration(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "小时" + mins + "分钟";
        } else {
            return mins + "分钟";
        }
    }
}
