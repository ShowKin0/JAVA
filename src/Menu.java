import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;
import java.io.IOException;
class person {
    AtomicInteger p = new AtomicInteger(0);
    LocalTime  start;
    long duration;          //一次开始到结束的时间
}

public class Menu {
    private static final Object scannerLock = new Object();

    public static void main(String[] args) {
        File record = new File("record.txt");
        startPrepare(record);
        person p=new person();
        Scanner sc = new Scanner(System.in);
           //  菜单循环开始
        while (true) {
            System.out.println("Enter number for interaction");
            System.out.println("1 to start,2 to end");
            int interaction;
            interaction = sc.nextInt();
            if (interaction ==1) {
                p.start = LocalTime.now();
                //   开始后每分钟报时间，直到输入2结束
                while (true) {
                    LocalTime end = LocalTime.now();
                    p.duration = (Duration.between(p.start, end).getSeconds() + 59) / 60;
                    System.out.println(end);
                    int Hours = (int) p.duration / 60;
                    int Minutes = (int) p.duration % 60;
                    System.out.println(Hours + ":" + Minutes);
                    Thread input = new Thread(() -> {
                        synchronized (scannerLock) {
                            p.p.set(sc.nextInt());
                        }
                    });
                    input.setDaemon(true);
                    input.start();
                    try {
                        input.join(60000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (input.isAlive()) {
                        input.interrupt();
                    }
                    if (p.p.get() == 2) {
                        System.out.println("Finally => "+Hours + ":" + Minutes);
                        break;
                    }else {System.out.println("input 2 to end");}
                }
            }
            else if (interaction ==2) {
                return;
            } else {System.out.println("input 1 to start");}
        }

    }
    public static void TodayAllStadyTime() {
        File record = new File("record.txt");

        //读取上次记录的时间，判断是否是今天
        //如果是今天，则计算时间
        //如果不是今天，则重新开始记录
        //记录时间
        //保存时间
    }
    public static void startPrepare(File record) {
        try{
            if(record.exists()){
                return;
            }else {

                record.createNewFile();
                FileWriter fw = new FileWriter(record,true);
                fw.write(LocalTime.now().toString()+"\n");
                fw.write(0+"\n");
                fw.write(0+"\n");
                fw.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
