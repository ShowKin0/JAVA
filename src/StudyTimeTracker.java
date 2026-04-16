import java.io.*;                    // 导入IO流相关的类，用于文件读写操作
import java.time.Duration;           // 导入Duration类，用于计算两个时间点之间的时长
import java.time.LocalDate;          // 导入LocalDate类，表示日期（年月日）
import java.time.LocalDateTime;      // 导入LocalDateTime类，表示日期和时间（年月日时分秒）
import java.time.LocalTime;          // 导入LocalTime类，表示时间（时分秒）
import java.time.format.DateTimeFormatter;  // 导入DateTimeFormatter类，用于格式化日期时间显示
import java.util.NoSuchElementException;    // 导入NoSuchElementException异常类，处理输入结束的情况
import java.util.Scanner;            // 导入Scanner类，用于从控制台读取用户输入
import java.util.concurrent.atomic.AtomicBoolean;  // 导入AtomicBoolean类，线程安全的布尔值
import java.util.concurrent.atomic.AtomicInteger;  // 导入AtomicInteger类，线程安全的整数

/**
 * 学习时间记录系统
 * 
 * 【核心对象功能解释】
 * 
 * 1. Scanner 对象 (scanner):
 *    - 功能: 从键盘读取用户输入
 *    - 创建: new Scanner(System.in)
 *    - 方法: nextLine() 读取一行输入，trim() 去掉首尾空格
 * 
 * 2. LocalDateTime 对象 (startTime, endTime, now):
 *    - 功能: 存储日期和时间信息
 *    - 创建: LocalDateTime.now() 获取当前系统时间
 *    - 方法: format() 按指定格式显示时间
 * 
 * 3. Duration 对象 (通过Duration.between()创建):
 *    - 功能: 计算两个时间点之间的时长
 *    - 创建: Duration.between(开始时间, 结束时间)
 *    - 方法: toMinutes() 转换为分钟数
 * 
 * 4. AtomicInteger 对象 (command):
 *    - 功能: 线程安全的整数，用于多线程间传递命令信号
 *    - 创建: new AtomicInteger(初始值)
 *    - 方法: set() 设置值，get() 获取值
 *    - 为什么用Atomic: 因为主线程和输入线程同时访问，需要线程安全
 * 
 * 5. AtomicBoolean 对象 (running):
 *    - 功能: 线程安全的布尔值，控制循环是否继续运行
 *    - 创建: new AtomicBoolean(初始值)
 *    - 方法: set() 设置值，get() 获取值
 * 
 * 6. Thread 对象 (inputThread):
 *    - 功能: 创建一个新线程，在后台执行代码
 *    - 创建: new Thread(Runnable任务)
 *    - 方法: start() 启动线程，interrupt() 中断线程
 *    - 作用: 后台监听用户输入，不阻塞主计时循环
 * 
 * 7. DateTimeFormatter 对象 (TIME_FORMATTER, DATE_FORMATTER):
 *    - 功能: 定义日期时间的显示格式
 *    - 创建: DateTimeFormatter.ofPattern("模式")
 *    - 模式: "HH:mm:ss" 时分秒, "yyyy-MM-dd" 年月日
 * 
 * 8. File 对象 (file):
 *    - 功能: 代表硬盘上的一个文件或目录
 *    - 创建: new File("文件路径")
 *    - 方法: exists() 检查是否存在，createNewFile() 创建新文件
 * 
 * 9. BufferedReader 对象 (reader):
 *    - 功能: 高效地从文件读取文本数据
 *    - 创建: new BufferedReader(new FileReader(文件))
 *    - 方法: readLine() 读取一行，返回null表示文件结束
 * 
 * 10. FileWriter 对象 (writer):
 *     - 功能: 向文件写入文本数据
 *     - 创建: new FileWriter(文件)
 *     - 方法: write() 写入字符串，close() 关闭文件
 * 
 * 11. StringBuilder 对象 (content):
 *     - 功能: 高效地拼接多个字符串
 *     - 创建: new StringBuilder()
 *     - 方法: append() 追加内容，toString() 转为String
 */
public class StudyTimeTracker {
    // 定义常量：记录文件的文件名
    // private: 只在当前类使用
    // static: 属于类而不是对象，可以直接用类名访问
    // final: 值不可修改，是常量
    private static final String RECORD_FILE = "study_record.txt";
    
    // 定义常量：时间格式化为 "时:分:秒" 格式，如 "14:30:25"
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // 定义常量：日期格式化为 "年-月-日" 格式，如 "2026-04-16"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 程序入口方法
     * public: 公开访问，JVM需要调用它
     * static: 静态方法，不需要创建对象就能调用
     * void: 不返回任何值
     * String[] args: 命令行参数数组
     */
    public static void main(String[] args) {
        // 调用初始化方法，检查并创建记录文件
        initializeRecordFile();
        
        // 创建Scanner对象，用于读取键盘输入
        // System.in 代表标准输入流（键盘）
        Scanner scanner = new Scanner(System.in);

        // 打印程序标题
        System.out.println("===== 学习时间记录系统 =====");
        // 调用getTodayStudyTime()获取今日学习时长，formatDuration()格式化为易读格式
        System.out.println("今日已学习: " + formatDuration(getTodayStudyTime()));
        // 打印空行，美化输出
        System.out.println();

        // while(true) 创建无限循环，程序持续运行直到用户选择退出
        while (true) {
            // 打印菜单选项
            System.out.println("请输入操作:");
            System.out.println("1 - 开始计时");
            System.out.println("2 - 退出程序");
            System.out.print("> ");  // 提示符，等待用户输入

            // 读取用户输入的一行文本，trim()去掉首尾空格
            String input = scanner.nextLine().trim();

            // switch 根据用户输入执行不同操作
            switch (input) {
                case "1":  // 如果输入是"1"
                    // 调用startStudySession方法开始计时，传入scanner对象
                    startStudySession(scanner);
                    break;  // 跳出switch，继续while循环
                case "2":  // 如果输入是"2"
                    // 打印退出信息和今日总学习时长
                    System.out.println("程序已退出，今日学习总时长: " + formatDuration(getTodayStudyTime()));
                    return;  // 结束main方法，程序退出
                default:   // 如果输入既不是"1"也不是"2"
                    // 提示输入无效，\n是换行符
                    System.out.println("无效输入，请输入 1 或 2\n");
            }
        }
    }

    /**
     * 开始学习计时会话
     * private: 只在当前类内部使用
     * static: 静态方法
     * 参数 scanner: 传入的Scanner对象，用于读取输入
     */
    private static void startStudySession(Scanner scanner) {
        // 获取当前系统日期时间，作为学习开始时间
        LocalDateTime startTime = LocalDateTime.now();
        
        // 打印计时开始信息，使用TIME_FORMATTER格式化时间显示
        System.out.println("\n>>> 计时开始于 " + startTime.format(TIME_FORMATTER));
        System.out.println("提示: 计时进行中，每分钟会显示当前学习时长");
        System.out.println("      输入 2 结束当前计时\n");

        // 创建AtomicInteger对象，初始值为0
        // 用于存储用户输入的命令（0表示无命令，2表示结束）
        AtomicInteger command = new AtomicInteger(0);
        
        // 创建AtomicBoolean对象，初始值为true
        // 用于控制循环是否继续运行
        AtomicBoolean running = new AtomicBoolean(true);

        // 创建后台线程，用于监听用户输入
        // 使用Lambda表达式定义线程要执行的任务
        Thread inputThread = new Thread(() -> {
            // while循环，只要running为true就继续监听
            while (running.get()) {
                try {
                    // 读取用户输入的一行
                    String line = scanner.nextLine().trim();
                    // 如果输入是"2"
                    if (line.equals("2")) {
                        // 将command设置为2，通知主线程结束计时
                        command.set(2);
                        // 跳出while循环，结束输入监听
                        break;
                    }
                } catch (NoSuchElementException e) {
                    // 如果输入流结束（如用户按Ctrl+C），跳出循环
                    break;
                }
            }
        });
        
        // 将线程设置为守护线程
        // 守护线程：当所有非守护线程结束时，守护线程自动结束
        // 这样主程序退出时，输入线程不会阻止程序退出
        inputThread.setDaemon(true);
        
        // 启动输入线程，开始后台监听
        inputThread.start();

        // 计数器，记录经过了多少秒
        int secondsCounter = 0;
        
        // 主循环：持续检查是否收到结束命令
        while (running.get()) {
            try {
                // 让当前线程睡眠1000毫秒（1秒）
                // 这样每秒检查一次，避免CPU占用过高
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // 如果线程被中断，恢复中断状态并跳出循环
                Thread.currentThread().interrupt();
                break;
            }

            // 检查command是否为2（用户输入了结束命令）
            if (command.get() == 2) {
                break;  // 跳出while循环，结束计时
            }

            // 秒数计数器加1
            secondsCounter++;
            // 如果累计满60秒（1分钟）
            if (secondsCounter >= 60) {
                // 重置计数器
                secondsCounter = 0;
                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();
                // 计算从开始到现在经过的分钟数
                long minutes = Duration.between(startTime, now).toMinutes();
                // 打印当前学习时长
                System.out.println("当前学习时长: " + formatDuration(minutes));
            }
        }

        // 设置running为false，通知输入线程停止
        running.set(false);
        // 中断输入线程，唤醒它（如果正在等待输入）
        inputThread.interrupt();

        // 获取当前时间作为结束时间
        LocalDateTime endTime = LocalDateTime.now();
        // 计算本次学习的分钟数
        long sessionMinutes = Duration.between(startTime, endTime).toMinutes();

        // 打印计时结束信息
        System.out.println("\n<<< 计时结束于 " + endTime.format(TIME_FORMATTER));
        System.out.println("本次学习时长: " + formatDuration(sessionMinutes));

        // 调用saveStudyRecord方法，将本次学习时长保存到文件
        saveStudyRecord(sessionMinutes);
        // 打印今日累计学习时长
        System.out.println("今日累计学习: " + formatDuration(getTodayStudyTime()));
        // 打印空行
        System.out.println();
    }

    /**
     * 初始化记录文件
     * 检查文件是否存在，不存在则创建
     */
    private static void initializeRecordFile() {
        // 创建File对象，代表记录文件
        File file = new File(RECORD_FILE);
        // 检查文件是否存在
        if (!file.exists()) {
            try {
                // 创建新文件
                file.createNewFile();
            } catch (IOException e) {
                // 如果创建失败，打印错误信息
                // System.err是标准错误输出流，通常显示为红色
                System.err.println("创建记录文件失败: " + e.getMessage());
            }
        }
    }

    /**
     * 保存学习记录到文件
     * 参数 minutes: 本次学习的分钟数
     */
    private static void saveStudyRecord(long minutes) {
        // 获取今天的日期，格式化为 "yyyy-MM-dd"
        String today = LocalDate.now().format(DATE_FORMATTER);
        // 计算今日总学习时长 = 已有时长 + 本次时长
        long todayTotal = getTodayStudyTime() + minutes;

        // 创建StringBuilder，用于拼接文件内容
        StringBuilder content = new StringBuilder();
        // 标记是否找到今天的记录
        boolean foundToday = false;

        // try-with-resources 语句，自动关闭资源
        // 创建BufferedReader读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(RECORD_FILE))) {
            String line;  // 存储读取的每一行
            // 循环读取，直到文件结束（readLine返回null）
            while ((line = reader.readLine()) != null) {
                // 检查当前行是否是今天的记录（以今天日期开头）
                if (line.startsWith(today + ",")) {
                    // 是今天的记录，更新为新的总时长
                    content.append(today).append(",").append(todayTotal).append("\n");
                    foundToday = true;  // 标记已找到
                } else {
                    // 不是今天的记录，原样保留
                    content.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            // 文件可能不存在或读取失败，忽略错误
            // 首次运行时文件可能为空，这是正常的
        }

        // 如果文件中没有今天的记录（今天是第一次学习）
        if (!foundToday) {
            // 添加今天的记录
            content.append(today).append(",").append(todayTotal).append("\n");
        }

        // 将更新后的内容写回文件
        try (FileWriter writer = new FileWriter(RECORD_FILE)) {
            // 写入StringBuilder的内容
            writer.write(content.toString());
        } catch (IOException e) {
            // 如果写入失败，打印错误信息
            System.err.println("保存记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日学习时长
     * 返回值: 今日学习的总分钟数，如果没有记录返回0
     */
    private static long getTodayStudyTime() {
        // 获取今天的日期字符串
        String today = LocalDate.now().format(DATE_FORMATTER);

        // 打开文件读取记录
        try (BufferedReader reader = new BufferedReader(new FileReader(RECORD_FILE))) {
            String line;
            // 逐行读取文件
            while ((line = reader.readLine()) != null) {
                // 按逗号分割行内容，得到日期和时长两部分
                String[] parts = line.split(",");
                // 检查格式是否正确（两部分）且日期是否匹配今天
                if (parts.length == 2 && parts[0].equals(today)) {
                    // 解析时长部分为长整数并返回
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            // IOException: 文件读取错误
            // NumberFormatException: 数字格式错误
            // 忽略这些错误，返回0
        }

        // 如果没有找到今天的记录，返回0
        return 0;
    }

    /**
     * 格式化时长显示
     * 参数 minutes: 分钟数
     * 返回值: 格式化后的字符串，如 "1小时30分钟" 或 "45分钟"
     */
    private static String formatDuration(long minutes) {
        // 计算小时数：整数除法，如 90 / 60 = 1
        long hours = minutes / 60;
        // 计算剩余分钟数：取余，如 90 % 60 = 30
        long mins = minutes % 60;
        // 如果小时数大于0
        if (hours > 0) {
            // 返回 "X小时Y分钟" 格式
            return hours + "小时" + mins + "分钟";
        } else {
            // 不足1小时，只返回分钟数
            return mins + "分钟";
        }
    }
}
